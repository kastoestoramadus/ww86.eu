# The Ursus by train page

What `lab/ursus-by-train/index.html` is for and the rules it follows, so a change does not have to
rediscover them. The page is in Polish; everything here applies to it. This file and
[ursus-by-train.py](ursus-by-train.py) sit directly in `lab/`, so `buildSite` does not publish them.

**Keep this file current.** After substantial work on the page, add an entry to the work log at the end
and update the rules the work changed, in the same PR. Skip it only when the user says the work is just
a check of a concept.

## Purpose

Places worth a trip that a person living in **Szamoty** (Ursus, Warsaw) reaches with **no change**:
museums, cinemas, monuments, parks, places to move, and shops. The starting points are fixed:

- the two railway stations, **Warszawa Ursus** (R1, S1) and **Warszawa Ursus Północny** (R3; the user
  calls it "Ursus Północ", it is by ul. Szamoty; it is not named "Szamoty");
- the nearest bus stops, **Gierdziejewskiego, Habicha and Lalki**, all within about 500 m.

The home address is not in this repository, which is public. The three stops are the reference point.

## Sections, in this order

1. **Frequent trains**, one column per line and direction, every station listed (minor ones as small
   dots) up to the terminus of the regular trains: R1 to Skierniewice, R3 to Łowicz, S1 to Otwock.
2. **Po drodze w Warszawie**: the stations all trains share in the centre. Each card lists every
   direct line that stops there with the ride time, trains and buses alike; a place near one of these
   stations lives here and nowhere else. Route columns point to it instead of repeating places.
3. **Buses** from the three stops, one column per line and direction, selected stops only.
4. **Rare trains**: direct, but a few times a day or on weekdays only. Only stations with places and zone
   borders, plus a "Tam / Z powrotem" timetable summary, because the direct return is often at a very
   different time or does not exist on the same day. Shown only when the line filter is "Wszystkie".

## Rules

- **Frequent** means every day, at least about ten departures a day also at weekends. Anything less
  goes to the rare section. Measured, as of September 2026: R1 to Skierniewice 25/21 (weekday/weekend
  day), R3 to Łowicz 14/12, S1 to Otwock 19/19; R2 to Mińsk Mazowiecki 13/3, R6 5–6/0, R7 to Pilawa
  6/0, to Dęblin 1/0, R2 to Siedlce 1/0.
- **Buses**: lines at the three stops that run often and leave Ursus. On the page: 517 (fast, every
  15 minutes also at weekends), 187 (goes far but slowly, about an hour to Stegny), 177 (Bemowo), 716
  (Monday to Saturday only, kept for the shops at both ends). Left out, and said so on the page: 207
  (loops within Ursus), 401 (weekdays only), N35 and N85 (night), 194 and 129 (stop further away, at
  PKP Ursus). The short hop of 517, 187 and 177 to the Ursus-Niedźwiadek loop is mentioned in the
  start stop's blurb, not given a column.
- **Ticket zones**: ZTM zone 1 is Warsaw; a zone 1+2 ticket is valid on KM and SKM trains up to
  Pruszków (R1), Płochocin (R3), Otwock Śródborów (R7; S1 ends at Otwock, inside zone 2), Sulejówek
  Miłosna (R2) and Zagościniec (R6).
  Border stations belong to both zones: Ursus-Niedźwiadek, Gołąbki, Falenica, Wola Grzybowska, Mokry
  Ług; on bus 716, Ursus - Sanktuarium. Mark a border with `zone:true` on the stop and the end of zone 2
  with `zEnd:true` on the last stop inside it. Source: the ZTM page on Wspólny Bilet ZTM-KM-WKD.
- **Places**: `c` is one of `kultura, rozrywka, aktywnie, zabytki, natura, zakupy`; `d` is `'at'` for a
  few minutes' walk (up to about 400 m), `null` for a walk, `'far'` for a bike or bus ride, with
  "Ok. N km od stacji." in the text. Measure the distance (Nominatim or OpenStreetMap against the
  station or the stop's GTFS coordinates), do not estimate it.
- **Every fact has a source** linked in the footer. Say what a place is and what is there; leave out
  claims no source backs. Opening hours go stale: give them only when a source states them, and prefer
  "sprawdź przed wizytą" for anything irregular. Check that a place still operates: the Ossów centre
  closed at the end of 2025 and the page says so.
- **Bus stops** use their GTFS coordinates as `q`, because a stop name alone is ambiguous in Google Maps.
- A new category needs a colour token in light and dark, a dot class and a filter chip.

## Refreshing the numbers

```bash
python3 lab/ursus-by-train.py
```

It downloads the GTFS feeds from https://mkuran.pl/gtfs/ (ZTM, and Koleje Mazowieckie with SKM),
picks a typical Tuesday, Saturday and Sunday (a one-off day such as a closure is skipped) and prints
departures per line, direction and terminus from both stations and the three stops, daytime gaps,
median ride times, and the direct trains there and back for the rare destinations. After a timetable
change (the Koleje Mazowieckie timetable on the page is valid until 24 October 2026) rerun it, update the `from` and
`when` texts, the hub ride times, the "stan:" date in the eyebrow and the footer note on counted days.

## Decisions taken with the user

- R1 and R3 were cut at Żyrardów and Sochaczew although most trains go on; they now run to the
  regular termini, Skierniewice and Łowicz.
- 716: at the Wola end it stops at the Fort Wola retail park (with a Kaufland), at the other end at
  Park Handlowy Piast in Piastów (Kaufland and more). Wola Park, which the user first named, is about
  1.9 km from the 716 terminus and is not on the page. From the Cm. Wolski terminus the walk to Park
  Sowińskiego, through Cmentarz Powstańców Warszawy and Park Powstańców Warszawy, is part of the
  attraction.
- 177 leaves from Habicha, on Sundays from Lalki (its regular Sunday route).

## Work log

### 2026-09-25: longer lines, zones, buses, rare trains, a new centre section

- R1 extended to Skierniewice and R3 to Łowicz (with Bednary for Nieborów and Arkadia); ticket zone
  borders marked; the lead and the card say the page is planned for Szamoty.
- New sections: buses (517, 187, 177, 716 both ways) and rare trains (R2, R6, R7). The centre section
  became one card per shared station listing every direct line with its ride time, trains and 517 alike;
  it follows the line filter and counts towards the total. Muzeum Narodowe added at Powiśle (320 m).
- A new category, Zakupy, for the retail parks at both ends of 716.
- Found on the way: the old page put the zone border at Michalin (it is Falenica); Saturday 26 September
  2026 ran a one-off bus timetable (no 716, 177 through Lalki), which is why the script picks typical
  days; the Ossów information centre closed on 31 December 2025.
- Checked: tests and `buildSite`, every external link answers 200, headless Chromium at 1280 and 360 px
  with no script errors and no horizontal scroll, every line filter. Not checked: the GTFS counts
  against KOLEO or the printed timetable; distances are straight lines, not walking routes.
