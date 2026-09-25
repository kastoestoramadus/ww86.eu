#!/usr/bin/env python3
"""Timetable numbers behind lab/ursus-by-train: how often trains and buses leave the home stations
and stops, where they go, how long the ride is. Rules for the page: lab/ursus-by-train.md.

    python3 lab/ursus-by-train.py [--cache DIR] [--weekday YYYYMMDD --saturday YYYYMMDD --sunday YYYYMMDD]

Downloads two GTFS feeds from https://mkuran.pl/gtfs/ into the cache (about 130 MB, reused on the next
run): polish_trains.zip (Koleje Mazowieckie, SKM and the rest) and warsaw.zip (ZTM buses). Standard
library only. Without dates it takes a typical Tuesday, Saturday and Sunday that both feeds cover.
"""
import argparse, collections, csv, datetime, io, os, statistics, urllib.request, zipfile

FEEDS = {'trains': 'https://mkuran.pl/gtfs/polish_trains.zip', 'ztm': 'https://mkuran.pl/gtfs/warsaw.zip'}
HOME_STATIONS = ('Warszawa Ursus', 'Warszawa Ursus Północny')
HOME_STOPS = ('Lalki', 'Habicha', 'Gierdziejewskiego')
CENTRE = ('Warszawa Śródmieście', 'Warszawa Powiśle', 'Warszawa Stadion', 'Warszawa Wschodnia')
FAR = ('Żyrardów', 'Skierniewice', 'Sochaczew', 'Łowicz Główny', 'Otwock')
RARE = ('Sulejówek', 'Mińsk Mazowiecki', 'Siedlce', 'Kobyłka-Ossów', 'Kobyłka', 'Wołomin', 'Pilawa', 'Dęblin')
BUS_TARGETS = {
    '517': ('Dw. Zachodni', 'Dw. Centralny', 'Centrum', 'Pl. Trzech Krzyży'),
    '187': ('Dw. Zachodni', 'Pomnik Lotnika', 'Metro Politechnika', 'Pl. Na Rozdrożu', 'Legia - Stadion', 'Stegny'),
    '177': ('Raginisa', 'Metro Bemowo', 'Os. Górczewska'),
    '716': ('Fort Wola', 'Cm. Wolski', 'Piastów Lelewela', 'Piastów Ogińskiego'),
}


def fetch(cache, name):
    path = os.path.join(cache, os.path.basename(FEEDS[name]))
    if not os.path.exists(path):
        os.makedirs(cache, exist_ok=True)
        print(f'downloading {FEEDS[name]}')
        urllib.request.urlretrieve(FEEDS[name], path)
    return zipfile.ZipFile(path)


def rows(z, name):
    with z.open(name) as f:
        yield from csv.DictReader(io.TextIOWrapper(f, encoding='utf-8-sig'))


def minutes(hhmm):
    return int(hhmm[:2]) * 60 + int(hhmm[3:5])


def services(z):
    days = collections.defaultdict(set)
    for r in rows(z, 'calendar_dates.txt'):
        if r['exception_type'] == '1':
            days[r['service_id']].add(r['date'])
    return days


def pick_days(feeds, args):
    """The earliest Tuesday, Saturday and Sunday running the timetable their weekday runs most often.
    A one-off day (a closure, a holiday) has services of its own, so it is never the most common."""
    if args.weekday:
        return {'wd': args.weekday, 'sat': args.saturday, 'sun': args.sunday}
    on_date = [collections.defaultdict(set) for _ in feeds]
    for by_date, svc in zip(on_date, feeds):
        for s, dates in svc.items():
            for d in dates:
                by_date[d].add(s)
    covered = set.intersection(*(set(b) for b in on_date))
    kinds = collections.defaultdict(list)
    for d in sorted(covered):
        key = {1: 'wd', 5: 'sat', 6: 'sun'}.get(datetime.date(int(d[:4]), int(d[4:6]), int(d[6:])).weekday())
        if key:
            kinds[key].append((d, tuple(frozenset(b[d]) for b in on_date)))
    picked = {}
    for key, dates in kinds.items():
        # Scored per feed: train services differ from date to date more often than ZTM ones.
        seen = [collections.Counter(sig[i] for _, sig in dates) for i in range(len(feeds))]
        picked[key] = max(dates, key=lambda ds: (sum(seen[i][ds[1][i]] for i in range(len(feeds))), -int(ds[0])))[0]
    return picked


def trips_through(z, stop_ids, trip_ids):
    """Stop sequences of the trips in `trip_ids` that call at one of `stop_ids`."""
    hit, seq = set(), collections.defaultdict(list)
    for r in rows(z, 'stop_times.txt'):
        if r['trip_id'] in trip_ids and r['stop_id'] in stop_ids:
            hit.add(r['trip_id'])
    for r in rows(z, 'stop_times.txt'):
        if r['trip_id'] in hit:
            seq[r['trip_id']].append(r)
    for s in seq.values():
        s.sort(key=lambda r: int(r['stop_sequence']))
    return seq


def trains(z, days):
    stops = {r['stop_id']: r for r in rows(z, 'stops.txt')}
    name = {sid: stops[r['parent_station'] or sid]['stop_name'] for sid, r in stops.items()}
    svc = services(z)
    routes = {r['route_id']: r['route_short_name'] for r in rows(z, 'routes.txt')}
    trips = {r['trip_id']: r for r in rows(z, 'trips.txt') if svc[r['service_id']] & set(days.values())}
    home = {sid for sid in stops if name[sid] in HOME_STATIONS}
    seq = trips_through(z, home, trips)

    def on(tid):
        return [k for k, d in days.items() if d in svc[trips[tid]['service_id']]]

    print('\n== Trains leaving the home stations: line, next stop, terminus, departures per day')
    count = collections.defaultdict(collections.Counter)
    ride = collections.defaultdict(list)
    for tid, st in seq.items():
        names = [name[r['stop_id']] for r in st]
        for i, r in enumerate(st[:-1]):
            if names[i] in HOME_STATIONS and r['pickup_type'] != '1':
                key = (names[i], routes[trips[tid]['route_id']], names[i + 1], names[-1])
                for k in on(tid):
                    count[key][k] += 1
                for j in range(i + 1, len(st)):
                    if names[j] in CENTRE + FAR:
                        ride[(names[i], names[j])].append(minutes(st[j]['arrival_time']) - minutes(r['departure_time']))
    for key in sorted(count, key=lambda k: (k[0], k[2], -count[k]['wd'])):
        c = count[key]
        print(f"  {key[0]:24} {key[1]:5} via {key[2]:27} to {key[3]:27} wd {c['wd']:3}  sat {c['sat']:3}  sun {c['sun']:3}")

    print('\n== Median ride in minutes')
    for (a, b), v in sorted(ride.items()):
        print(f'  {a:24} -> {b:22} {statistics.median(v):5}')

    print('\n== Rare destinations: direct trains there and back (U = Ursus, UP = Ursus Północny)')
    short = {'Warszawa Ursus': 'U', 'Warszawa Ursus Północny': 'UP'}
    for dest in RARE:
        for label, frm, to in (('there', HOME_STATIONS, (dest,)), ('back', (dest,), HOME_STATIONS)):
            got = collections.defaultdict(list)
            for tid, st in seq.items():
                names = [name[r['stop_id']] for r in st]
                i = next((i for i, n in enumerate(names) if n in frm and st[i]['pickup_type'] != '1'), None)
                j = next((j for j in range(i + 1, len(names)) if names[j] in to and st[j]['drop_off_type'] != '1'), None) if i is not None else None
                if j is None:
                    continue
                at = short.get(names[i]) or short.get(names[j])
                for k in on(tid):
                    got[k].append(f"{st[i]['departure_time'][:5]}({at})")
            print(f'  {dest:17} {label:5} ' + ' | '.join(f"{k} {len(got[k])}: {' '.join(sorted(got[k]))}" for k in days))


def buses(z, days):
    stops = {r['stop_id']: r for r in rows(z, 'stops.txt')}
    svc = services(z)
    routes = {r['route_id']: r['route_short_name'] for r in rows(z, 'routes.txt')}
    trips = {r['trip_id']: r for r in rows(z, 'trips.txt') if svc[r['service_id']] & set(days.values())}
    home = {sid for sid, r in stops.items() if r['stop_name'] in HOME_STOPS}
    seq = trips_through(z, home, trips)

    print(f"\n== Buses at {', '.join(HOME_STOPS)}: departures per day, daytime (10-18) gaps, median ride")
    count = collections.defaultdict(lambda: collections.defaultdict(list))
    ride = collections.defaultdict(list)
    for tid, st in seq.items():
        t = trips[tid]
        line = routes[t['route_id']]
        names = [stops[r['stop_id']]['stop_name'] for r in st]
        i = next((i for i, r in enumerate(st[:-1]) if r['stop_id'] in home and r['pickup_type'] != '1'), None)
        if i is None:
            continue
        dep = minutes(st[i]['departure_time'])
        for k, d in days.items():
            if d in svc[t['service_id']]:
                count[(line, t['trip_headsign'], names[i])][k].append(dep)
        if 600 <= dep < 1080:
            for j in range(i + 1, len(st)):
                if names[j] in BUS_TARGETS.get(line, ()):
                    ride[(line, names[i], names[j])].append(minutes(st[j]['departure_time']) - dep)
    for key in sorted(count, key=lambda k: (k[0].zfill(4), k[1])):
        out = []
        for k in days:
            ts = sorted(count[key][k])
            day = [m for m in ts if 600 <= m < 1080]
            gaps = [b - a for a, b in zip(day, day[1:])]
            out.append(f"{k} {len(ts):3}" + (f" gap {min(gaps)}-{max(gaps)}" if gaps else ''))
        print(f'  {key[0]:4} to {key[1]:30} from {key[2]:18} ' + ' | '.join(out))
    for (line, a, b), v in sorted(ride.items()):
        print(f'  {line:4} {a:18} -> {b:20} {statistics.median(v):5} min')


def main():
    p = argparse.ArgumentParser(description=__doc__.split('\n')[0])
    p.add_argument('--cache', default=os.path.join(os.path.expanduser('~'), '.cache', 'ww86-gtfs'))
    p.add_argument('--weekday')
    p.add_argument('--saturday')
    p.add_argument('--sunday')
    args = p.parse_args()
    zt, zb = fetch(args.cache, 'trains'), fetch(args.cache, 'ztm')
    days = pick_days([services(zt), services(zb)], args)
    print('days:', days)
    trains(zt, days)
    buses(zb, days)


if __name__ == '__main__':
    main()
