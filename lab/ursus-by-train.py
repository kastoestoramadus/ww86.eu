#!/usr/bin/env python3
"""Timetable numbers behind lab/ursus-by-train: how often trains and buses leave the home stations
and stops, where they go, how long the ride is. Rules for the page: lab/ursus-by-train.md.

    python3 lab/ursus-by-train.py [--cache DIR] [--weekday YYYYMMDD --saturday YYYYMMDD --sunday YYYYMMDD] [--map]

Downloads two GTFS feeds from https://mkuran.pl/gtfs/ into the cache (about 130 MB, reused on the next
run): polish_trains.zip (Koleje Mazowieckie, SKM and the rest) and warsaw.zip (ZTM buses). Standard
library only. Without dates it takes a typical Tuesday, Saturday and Sunday that both feeds cover.
With --map it prints no numbers and writes lab/ursus-by-train/routes.js instead: the path of every route
column on the page, cut from the feeds' shapes, and the stations each train calls at.
"""
import argparse, collections, csv, datetime, io, json, math, os, re, statistics, urllib.request, zipfile

FEEDS = {'trains': 'https://mkuran.pl/gtfs/polish_trains.zip', 'ztm': 'https://mkuran.pl/gtfs/warsaw.zip'}
HOME_STATIONS = ('Warszawa Ursus', 'Warszawa Ursus Północny')
HOME_STOPS = ('Lalki', 'Habicha', 'Gierdziejewskiego', 'Ursus - Ratusz')
CENTRE = ('Warszawa Włochy', 'Warszawa Ochota', 'Warszawa Śródmieście', 'Warszawa Powiśle', 'Warszawa Stadion',
          'Warszawa Wschodnia')
FAR = ('Żyrardów', 'Skierniewice', 'Sochaczew', 'Łowicz Główny', 'Otwock')
RARE = ('Sulejówek', 'Mińsk Mazowiecki', 'Siedlce', 'Kobyłka-Ossów', 'Kobyłka', 'Wołomin', 'Pilawa', 'Dęblin')
BUS_TARGETS = {
    '517': ('Berestecka', 'Dw. Zachodni', 'Pl. Zawiszy', 'Dw. Centralny', 'Centrum', 'Pl. Trzech Krzyży'),
    '187': ('Dw. Zachodni', 'Pomnik Lotnika', 'Metro Politechnika', 'Pl. Na Rozdrożu', 'Legia - Stadion', 'Stegny'),
    '177': ('Raginisa', 'Metro Bemowo', 'Os. Górczewska'),
    '716': ('Fort Wola', 'Cm. Wolski', 'Piastów Lelewela', 'Piastów Ogińskiego'),
    '207': ('Hassa',),
    '401': ('Orzechowa', 'Rondo Unii Europejskiej', 'Metro Służew'),
    '194': ('PKP Włochy', 'Fort Wola', 'Cm. Wolski', 'PKP Gołąbki'),
    '220': ('Hassa', 'Kolumba', 'P+R Al. Krakowska', 'Os. Górczewska', 'Kocjana', 'Piastów Śląskich', 'Nowe Bemowo'),
    '228': ('PKP Włochy', 'Rakowska', 'Jadwisin', 'Woronicza', 'Metro Wierzbno'),
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


PAGE = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'ursus-by-train', 'index.html')
ROUTES_JS = os.path.join(os.path.dirname(PAGE), 'routes.js')
TOLERANCE = 8  # metres a simplified path may stray from the shape
# Metres from a shape at which a station or a stop counts as passed; a bus shape can end some 40 m short
# of the terminus pole the page gives (177 at Os. Górczewska).
NEAR = {'train': 200, 'bus': 50}
K = math.cos(math.radians(52.2))  # a degree of longitude in degrees of latitude, around Warsaw


def page_lines():
    """The route columns of the page as (id, line, is_bus, [(stop, q)]), a bus stop's q being its
    coordinates, and the Po drodze cards as (station, [line ids]). UrsusSuite reads the same layout."""
    with open(PAGE, encoding='utf-8') as f:
        page = f.read()

    def block(name):
        start = page.index(f'var {name} = [')
        return page[start:page.index('\n  ];', start)]

    lines = []
    for name, bus in (('LINES', False), ('BUSES', True), ('RARE', False)):
        for entry in block(name).split("\n    { id:'")[1:]:
            line = re.search(r"f:'([^']*)'" if bus else r"badge:'([^']*)'", entry).group(1)
            stops = re.findall(r"^      \{ n:'([^']*)', q:'([^']*)'", entry, re.M)
            lines.append((entry[:entry.index("'")], line, bus, stops))
    hub = [(m.group(1), re.findall(r"'([^']*)'", m.group(2)))
           for m in re.finditer(r"^    \{ n:'([^']*)', q:'[^']*', lines:\[([^\]]*)\]", block('HUB'), re.M)]
    return lines, hub


def xy(p):
    return p[1] * K * 111320, p[0] * 111320


def nearest(p, a, b):
    """Metres from `p` to the segment a-b, and the point of the segment nearest to `p`."""
    (px, py), (ax, ay), (bx, by) = xy(p), xy(a), xy(b)
    dx, dy = bx - ax, by - ay
    squared = dx * dx + dy * dy
    t = 0 if squared == 0 else max(0, min(1, ((px - ax) * dx + (py - ay) * dy) / squared))
    return math.hypot(px - ax - t * dx, py - ay - t * dy), (a[0] + t * (b[0] - a[0]), a[1] + t * (b[1] - a[1]))


def passes(p, path, start, near):
    """Where `path` first passes `p` within `near` metres, from segment `start` on: (segment, point), the
    point nearest to `p` on that pass. None if it never does. A loop may come back to a stop later; the
    first pass is the one in the order of the stops."""
    found = None
    for i in range(start, len(path) - 1):
        d, q = nearest(p, path[i], path[i + 1])
        if d <= near and (found is None or d < found[0]):
            found = (d, i, q)
        elif found is not None and d > near:
            break
    return found and found[1:]


def cut(path, points, near):
    """`path` from where it passes the first of `points` to where it passes the last, having passed the
    rest in order; None if it misses one."""
    at, start = [], 0
    for p in points:
        hit = passes(p, path, start, near)
        if hit is None:
            return None
        at.append(hit)
        start = hit[0]
    (i, first), (j, last) = at[0], at[-1]
    return [first] + path[i + 1:j + 1] + [last]


def simplify(path):
    """Douglas-Peucker: the vertices `path` needs to stay within TOLERANCE of itself."""
    keep, todo = {0, len(path) - 1}, [(0, len(path) - 1)]
    while todo:
        a, b = todo.pop()
        worst, at = 0, None
        for i in range(a + 1, b):
            d = nearest(path[i], path[a], path[b])[0]
            if d > worst:
                worst, at = d, i
        if worst > TOLERANCE:
            keep.add(at)
            todo += [(a, at), (at, b)]
    return [path[i] for i in sorted(keep)]


def shapes(z, ids):
    points = collections.defaultdict(list)
    for r in rows(z, 'shapes.txt'):
        if r['shape_id'] in ids:
            points[r['shape_id']].append((int(r['shape_pt_sequence']), float(r['shape_pt_lat']), float(r['shape_pt_lon'])))
    return {s: [(lat, lon) for _, lat, lon in sorted(pts)] for s, pts in points.items()}


def train_routes(z, days, lines, hub):
    """A train's route runs from its home station to the end of its column, and a line on a Po drodze card
    starts at the last card's station instead, so the map shows it crossing the centre."""
    # The feed writes some names with a space where the page has a hyphen (Warszawa Ursus Niedźwiadek);
    # the map uses the page's spelling.
    spelling = {n.replace('-', ' '): n for _, _, bus, s in lines if not bus for n, _ in s}
    stops = {r['stop_id']: r for r in rows(z, 'stops.txt')}
    for r in stops.values():
        r['stop_name'] = spelling.get(r['stop_name'].replace('-', ' '), r['stop_name'])
    station = {sid: stops[r['parent_station'] or sid] for sid, r in stops.items()}
    svc = services(z)
    names = {r['route_id']: r['route_short_name'] for r in rows(z, 'routes.txt')}
    wanted = {line for _, line, bus, _ in lines if not bus}
    trips = {r['trip_id']: r for r in rows(z, 'trips.txt')
             if names[r['route_id']] in wanted and svc[r['service_id']] & set(days.values())}
    calls = collections.defaultdict(list)
    for r in rows(z, 'stop_times.txt'):
        if r['trip_id'] in trips:
            calls[r['trip_id']].append((int(r['stop_sequence']), station[r['stop_id']]))
    calls = {t: [s for _, s in sorted(c)] for t, c in calls.items()}

    # A column may join trips: most R2 trains from Ursus Północny end at Mińsk, the one to Siedlce starts
    # at Ursus. Each piece is the most common shape among the trips reaching furthest down the column.
    chosen = {}
    for lid, line, bus, page_stops in lines:
        if bus:
            continue
        want = [n for n, _ in page_stops]
        if any(lid in ids for _, ids in hub) and hub[-1][0] not in want:
            want = [hub[-1][0]] + want
        pieces, i = [], 0
        while i < len(want) - 1:
            reach = collections.defaultdict(list)  # (last station reached, shape) -> trips
            for t, c in calls.items():
                seen = [s['stop_name'] for s in c]
                if names[trips[t]['route_id']] != line or want[i] not in seen:
                    continue
                rest, j = iter(seen[seen.index(want[i]):]), i
                while j + 1 < len(want) and want[j + 1] in rest:
                    j += 1
                if j > i:
                    reach[(j, trips[t]['shape_id'])].append(t)
            if not reach:
                raise SystemExit(f'{lid}: no {line} trip from {want[i]} calls at {want[i + 1]}')
            (j, shape), ts = max(reach.items(), key=lambda kv: (kv[0][0], len(kv[1])))
            seen = [s['stop_name'] for s in calls[ts[0]]]
            pieces.append((shape, calls[ts[0]][seen.index(want[i]):seen.index(want[j]) + 1]))
            i = j
        chosen[lid] = pieces

    paths = shapes(z, {s for pieces in chosen.values() for s, _ in pieces})
    out = {}
    for lid, pieces in chosen.items():
        path, on = [], []
        for shape, calls_on in pieces:
            points = [(float(s['stop_lat']), float(s['stop_lon'])) for s in calls_on]
            piece = cut(paths[shape], points, NEAR['train'])
            if piece is None:
                raise SystemExit(f'{lid}: shape {shape} misses a station')
            path += piece[1:] if path else piece
            on += calls_on[1:] if on else calls_on
        path = simplify(path)
        placed, start = [], 0
        for s in on:
            start, q = passes((float(s['stop_lat']), float(s['stop_lon'])), path, start, NEAR['train'])
            placed.append((s['stop_name'], q))
        out[lid] = (placed, path)
    return out


def bus_routes(z, lines):
    """A bus route is the most common shape of its line that passes the page's stops in order."""
    names = {r['route_id']: r['route_short_name'] for r in rows(z, 'routes.txt')}
    wanted = {line for _, line, bus, _ in lines if bus}
    used = collections.Counter((names[r['route_id']], r['shape_id']) for r in rows(z, 'trips.txt')
                               if names.get(r['route_id']) in wanted)
    paths = shapes(z, {s for _, s in used})
    out = {}
    for lid, line, bus, page_stops in lines:
        if not bus:
            continue
        points = [tuple(map(float, q.split(','))) for _, q in page_stops]
        for (l, shape), _ in used.most_common():
            path = cut(paths[shape], points, NEAR['bus']) if l == line else None
            if path:
                out[lid] = (None, simplify(path))
                break
        else:
            raise SystemExit(f'{lid}: no shape of {line} passes all its stops')
    return out


def route_map(zt, zb, days):
    lines, hub = page_lines()
    routes = {**train_routes(zt, days, lines, hub), **bus_routes(zb, lines)}
    fmt = lambda p: [round(p[0], 5), round(p[1], 5)]
    with open(ROUTES_JS, 'w', encoding='utf-8') as f:
        f.write('// Written by lab/ursus-by-train.py --map from the GTFS feeds of https://mkuran.pl/gtfs/ (Koleje\n'
                '// Mazowieckie, SKM, ZTM Warszawa); edit the page and run it again. A route is the path of a column on\n'
                '// the page; a train\'s route also lists the stations it calls at, [name, lat, lon], placed on the path.\n'
                'var ROUTES = {\n')
        for n, (lid, *_) in enumerate(lines):
            placed, path = routes[lid]
            f.write(f'  {lid}: {{\n')
            if placed is not None:
                f.write('    stops: ' + json.dumps([[s] + fmt(p) for s, p in placed], ensure_ascii=False, separators=(',', ':')) + ',\n')
            f.write('    path: ' + json.dumps([fmt(p) for p in path], separators=(',', ':')) + '\n')
            f.write('  }' + (',' if n < len(lines) - 1 else '') + '\n')
        f.write('};\n')
    for lid, (placed, path) in routes.items():
        print(f'{lid:6} {len(path):4} points' + (f', {len(placed)} stations' if placed else ''))
    print('wrote', ROUTES_JS)


def main():
    p = argparse.ArgumentParser(description=__doc__.split('\n')[0])
    p.add_argument('--cache', default=os.path.join(os.path.expanduser('~'), '.cache', 'ww86-gtfs'))
    p.add_argument('--weekday')
    p.add_argument('--saturday')
    p.add_argument('--sunday')
    p.add_argument('--map', action='store_true', help='write lab/ursus-by-train/routes.js instead of the numbers')
    args = p.parse_args()
    zt, zb = fetch(args.cache, 'trains'), fetch(args.cache, 'ztm')
    days = pick_days([services(zt), services(zb)], args)
    print('days:', days)
    if args.map:
        route_map(zt, zb, days)
        return
    trains(zt, days)
    buses(zb, days)


if __name__ == '__main__':
    main()
