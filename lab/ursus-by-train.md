# The Ursus by train page

What `lab/ursus-by-train/index.html` is for and the rules it follows. The page is in Polish. This file and
[ursus-by-train.py](ursus-by-train.py) sit directly in `lab/`, so `buildSite` does not publish them.
**Keep it current:** substantial work updates the rules it changed and adds a line to the work log, in
the same PR (skip only when the user calls the work a check of a concept).

## Purpose

Places worth a trip that a person living in **Szamoty** (Ursus, Warsaw) reaches with **no change**. Fixed
starting points: the stations **Warszawa Ursus** (R1, S1) and **Warszawa Ursus Północny** (R3; the user
says "Ursus Północ", it is by ul. Szamoty but is not named after it), the bus stops **Gierdziejewskiego,
Habicha and Lalki**, within about 500 m, and, at the user's request, **Ursus - Ratusz**, 0.9–1.2 km from
Lalki (its four poles). The home address stays out of this public repository.

**Nothing within a 20-minute walk of Szamoty** (the user, 2026-09-26): that is no trip. Measured from the
centre of Szamoty, the OSM node of the quarter (node 410515045, not the edge of the MSI area: the user
corrected that the same day, after the edge had cut five places), on OSM footpaths with the FOSSGIS foot
router (`routing.openstreetmap.de/routed-foot`). Checked against a walk the user knows: the router gives
42–47 minutes from the home stops to Netto at Kleszczowa 18, the user walks it in 46. Nothing on the page
is that near; the nearest are Albatros (24 min), Mini Zoo (27), Kino ADA (28), Skalar and Park Linowy
(29), Park Kombatantów (32).

## Sections, in this order

1. **Map**, under the filters: every route column as a line, with no places on it. Frequent trains are thick
   in their colours with every station they call at, R1 and R3 drawn from Warszawa Wschodnia because the
   Po drodze cards have them cross the centre; buses thin, with the stops their columns list; rare
   trains dashed, with their columns' stations, only under "Wszystkie". A line filter dims the rest and
   brings its line into view. Paths and station positions come from `routes.js` (see Refreshing), the
   tiles from OpenStreetMap, credited on the map and in the footer.
2. **Frequent trains**: a column per line and direction, every station (minor ones as small dots), up to
   the regular terminus: R1 Skierniewice, R3 Łowicz, S1 Otwock.
3. **Po drodze w Warszawie**: the stations every train from both home stations stops at, Włochy to
   Wschodnia. A card lists each direct line with its ride time, trains and buses alike; a place near such
   a station lives only here, and route columns point to it. Zachodnia's card holds only the Aldi under
   its platforms; Blue City and Reduta (1–1.2 km) sit at the Berestecka stop of 517 and 187.
4. **Buses** from the three stops and from Ursus - Ratusz: a column per line and direction, selected stops only.
5. **Rare trains**: direct but a few times a day or weekdays only; stations with places and zone borders
   only, a "Tam / Z powrotem" summary, shown only under the "Wszystkie" line filter.

## Rules

- **Frequent** = daily, about ten departures a day or more also at weekends; the rest is rare. September
  2026, weekday/weekend: R1 Skierniewice 25/21, R3 Łowicz 14/12, S1 Otwock 19/19; R2 Mińsk 13/3, R6
  5–6/0, R7 Pilawa 6/0, Dęblin 1/0, R2 Siedlce 1/0.
- **Buses** on the page and why: 517 (fast, every 15 min also at weekends), 187 (far but slow), 177
  (Bemowo), 716 (Mon–Sat, for the shops at both ends), 207 (loops within Ursus, but stops 160–240 m from
  Centrum Skorosze and Leroy Merlin, where 517 is 700 m away), 401 (weekdays only; the only direct line to
  Centrum Łopuszańska 22, 517 and 187 stop 1.5 km away). The GTFS ends 401 at Metro Wilanowska, a stop with
  no boarding; its passenger terminus is Metro Służew. From Ursus - Ratusz: 194 to Włochy, Fort Wola and
  Cm. Wolski (also on Sundays, when 716 does not run), 220 both ways (Okęcie, Bemowo), 228 to Rakowiec and
  Mokotów; 194 and 228 stop at PKP Włochy, so its hub card lists them. 194 the other way stays in Ursus and
  is a sentence in the start stop's blurb. Left out, and said so on the page: N35, N85 (night), 129 (it
  stops at PKP Ursus). The hop of 517, 187 and 177 to the Ursus-Niedźwiadek loop is a
  sentence in the start stop's blurb.
- **Ticket zones**: zone 1 is Warsaw; a 1+2 ticket is valid on KM and SKM trains up to Pruszków (R1),
  Płochocin (R3), Otwock Śródborów (R7; S1 ends at Otwock, in zone 2), Sulejówek Miłosna (R2), Zagościniec
  (R6). Border stops belong to both: Ursus-Niedźwiadek, Gołąbki, Falenica, Wola Grzybowska, Mokry Ług, and
  Ursus - Sanktuarium on 716. `zone:true` marks a border, `zEnd:true` the last stop inside zone 2.
- **A place** is `{ n, q, c, d, t }`. `q` is a Google Maps query; bus stops use their GTFS coordinates,
  because a stop name is ambiguous. `d` is `'at'` up to about 400 m, `null` for a walk, `'far'` for a bike
  or bus ride with "Ok. N km od stacji." in `t`; give the distance in `t` for walks of a kilometre or more
  too. Measure distances (OSM or GTFS coordinates), never estimate them.
- **Categories** (`c`): a list of one or two, the most important first; the dots follow its order, and
  a filter finds a place by either. A second one only when a visitor filtering by it would want the place
  and the text backs it; a place that needs three is split into entries, if it consists of parts with
  their own name or entrance (the user, 2026-09-25).
  - `kultura`: museums (railway ones too), theatres (PKiN's), galleries, cultural centres, also those with
    occasional screenings (MOK Józefów, Stare Kino in Milanówek);
  - `kino`: cinemas with a regular programme, studio and community ones included, and a cultural centre
    that runs one is a single `['kino','kultura']` entry (Kino Bajka in CK Błonie). A museum or palace
    hosting a cinema keeps its own entry beside it: MSN and KinoMuzeum, PKiN and Kinoteka, Zamek
    Ujazdowski and Kino U-jazdowski; a multiplex whose text is about its mall is also `zakupy`;
  - `mecze` (label "Mecze i koncerty"): where you watch a match or a big concert: stadiums, Torwar,
    concert clubs and halls for about a thousand people or more (Progresja, Stodoła, Palladium, the
    Filharmonia, Amfiteatr Bemowo). Sala Kongresowa comes back when its renovation ends (mid-2028 at
    the earliest);
  - `aktywnie` (doing sport: pools, tracks, trampolines), `zabytki` (a building or site worth seeing for
    itself, not a museum about history), `natura`, `zakupy`.
  There is no catch-all: a place that fits none is reported to the user, who names a new category.
  A category needs a label in `CAT`, a colour in the light and both dark blocks, a dot class and a chip.
  `gen/.../UrsusSuite` checks that, and files cinemas by name (Kino, Multikino, Cinema City, Helios) first
  under `kino`, with `occasional` as the list of names that sound like a cinema but screen rarely, and
  stadiums (Stadion, Narodowy) under `mecze`.
- **Shops**: malls only when they hold a cinema or a hypermarket, or the user asked. **Of one chain at
  most three big stores, the nearest by ride time**, and besides them **one of its biggest**: its
  biggest within 30 minutes' ride, or, when the biggest on the lines is farther, a closer one; its text
  says so (Kaufland in Fort Wola, 5,000 m²). Ride time counts from the stop or station the line starts
  at, as the columns give it. A mall or retail park listed for its hypermarket counts for that chain
  (`UrsusSuite` counts the chains named in `zakupy` places). **Big** means a hypermarket (Kaufland,
  E.Leclerc, Carrefour, Auchan) within about 1.2 km, or another store from about **1,300 m²** of sales
  floor, adjacent stores summed: a few small ones side by side count as one hypermarket (the user,
  2026-09-26; it was 2,000 m², single stores). Not: Carrefour Market or Express, Moje Auchan; checked and
  left out, e.g. Carrefour in Nowa Stacja Pruszków (900 m²), in Józefów, Auchan in Brwinów and all three
  in Ursus. Carrefour is closing hypermarkets (93 left in mid-2026): check that one still trades. In:
  the Eurospar in Blue City (2,500 m²), the Selgros halls (any adult gets the customer card); Makro
  serves businesses only. **Aldi and Netto are always on the page**, the three nearest of each whatever
  their size; **Lidl, Biedronka and Stokrotka never**: the user walks to them (both the user,
  2026-09-26; `UrsusSuite` checks it). No store of the other discounters reaches 1,300 m² in a source
  (Aldi's largest in Poland has 1,300 m², Biedronka's 1,800 m² is in Łódź).
- **Pools**: every public pool open now within about 1.5 km of a station or stop, and outdoor ones that
  ran their summer season. Left out: pools closed for works (Jagiellońska 7 since May 2026, Kawęczyńska
  36), Milanówek's outdoor pool (shut since 2024), the university pool on Banacha (students only).
  Otwock's new pool (ul. Karczewska, 1.9 km from the station) was to open in the second half of September
  2026: add it once it does. A pool's text puts **a sauna in bold** when it has one, names what few pools
  have (a wave pool, a long slide, a 50 m pool, a diving tower, a lazy river, a salt grotto) and warns,
  in bold too, when it is **closed at weekends** (the user, 2026-09-26). `**…**` in `t` is the page's
  only markup: it renders as bold, and `UrsusSuite` fails on a sauna that is not.
- **Seasonal**: a place whose attraction runs part of the year only (a bathing beach, an outdoor pool, a
  rink) carries `s`, a few words on what and when, shown as a chip after the categories ("sezonowo: …").
- **Every fact has a source** in the footer; say what a place is and has, nothing a source does not
  back. Hours only when a source gives them, "sprawdź przed wizytą" for anything irregular. Check that a
  place still operates (the Ossów centre closed at the end of 2025; starekinomilanowek.pl became a parked
  domain). The Leroy Merlin canteens carry the user's own recommendation, in the first person like the
  rest of the page.

## Finding and checking places

- **Candidates**: an Overpass query for `amenity=cinema`, `shop=mall`, hypermarket brands and trampoline
  parks within 1.5 km of every station and stop, then each hit checked on the web. OSM misses cinemas run
  by cultural centres (Kino Bajka, Kino ADA), so search the towns by name as well. Found nothing, September
  2026: a cinema in Pruszków besides Multikino, in Brwinów, Ożarów, and in Bemowo (outdoor only). Pools:
  `leisure=sports_centre|water_park|swimming_pool` with `sport=swimming` per bounding box (an `around`
  over every stop times out), then the towns by name: OSM misses ICSiR in Józefów and the Brwinów pool
  (not found yet). Warsaw's city pools each have a page on `sport.um.warszawa.pl` with hours and breaks.
  Store sizes: press notes on openings give the sales floor; an OSM building footprint is a fair proxy
  only for a standalone store, and ranks a chain's stores well enough to know which one to look up. For
  discounters the notes rarely give a size (Lidl, Biedronka, Netto); chain-wide figures do (dlahandlu.pl,
  August 2026: Lidl 1,000–1,400 m² a store, Biedronka 600–700 m²).
- **Walking times** from Szamoty: the FOSSGIS router's `table` service from node 410515045 to the place;
  check a new measure against a walk the user knows before cutting anything with it.
- **Nominatim and Overpass** want a User-Agent; send a neutral one (`ww86-research/1.0`), never a
  personal e-mail address.
- **Regular or occasional** screenings: count the dates on the venue's programme (Kino ADA: 17 days in a
  month; MOK Józefów: a few a month, some months none).
- **Site quirks**: Leroy Merlin store URLs change (OSM's link for Al. Jerozolimskie 244 is a 404, the
  current page is `warszawa-al-jerozolimskie-gigamarket.html`) and the store pages do not list the
  canteens, their Facebook pages do. Carrefour store pages do not state the format; the name does
  ("Carrefour Market …" is a supermarket). Sites that refuse scripts: see the browser note in
  [AGENTS.md](../AGENTS.md).
- **Before a PR**: `sbt test buildSite`, every new link answers, the page in a browser at 1280 and 360 px
  with no script error or horizontal scroll under every line and category filter.

## Refreshing the numbers

```bash
python3 lab/ursus-by-train.py
```

Downloads the ZTM and Koleje Mazowieckie/SKM GTFS feeds from https://mkuran.pl/gtfs/, picks a typical
Tuesday, Saturday and Sunday (skipping one-off days), and prints departures per line, direction and
terminus, daytime gaps, median ride times to the hub stations and the bus stops with places, and the
direct trains there and back for the rare destinations. After a timetable change (the KM timetable on the
page runs until 24 October 2026) update the `from` and `when` texts, the hub ride times, the "stan:" date
and the footer note on counted days.

```bash
python3 lab/ursus-by-train.py --map
```

Rewrites `lab/ursus-by-train/routes.js` from the same feeds: each column's path, cut from the shape of
its most common trip (or of the trips it joins: the only R2 to Siedlce starts at Ursus, the others from
Ursus Północny end at Mińsk), and the stations each train calls at. Run it after changing a route column;
`UrsusSuite` fails while the map misses a line or a station, or a route no longer passes a stop.

## Decisions taken with the user

- R1 and R3 run to their regular termini, Skierniewice and Łowicz, not to Żyrardów and Sochaczew.
- 716 stops at Fort Wola (with a Kaufland) and at Park Handlowy Piast in Piastów; Wola Park, 1.9 km from
  its terminus, came in with 220 (1 km from Orlich Gniazd) as one of Auchan's three. From Cm. Wolski the
  walk to Park Sowińskiego through the two cemetery parks is part of the attraction. 177 leaves from
  Habicha, on Sundays from Lalki.
- Cinemas "even studio ones, but open"; Centrum Skorosze, Blue City, Reduta and Centrum Łopuszańska 22 by
  name; hypermarkets only big; the Leroy Merlin canteens at Skorosze and on 716 north; cinemas as their
  own category; a cultural centre with a regular cinema is `kino`, with occasional screenings `kultura`.
- The map shows where the connections reach and nothing else: no places, the list has them.
- The buses from Ursus - Ratusz, though farther than the other stops: the user asked for them.
- Rozrywka, the catch-all, is dropped. What was left went to `kultura` (the railway museums, PKiN's
  theatres, Koneser) and to a new `mecze`, "Mecze i koncerty", for the stadiums: the user's suggestion.
- 2026-09-25: at most three hypermarkets of a chain, the nearest; the biggest stores of every chain,
  when comparable with a big supermarket; one or two categories a place, split what needs three; Torwar
  and the other big concert venues; every operating pool.
- 2026-09-26: big stores from 1,300 m², adjacent stores summed; besides a chain's three nearest, one of
  its biggest within 30 minutes' ride; Aldi and Netto always, Lidl, Biedronka and Stokrotka never;
  nothing within a 20-minute walk of the centre of Szamoty, so no "W Ursusie" section either; saunas in
  bold, rare attractions and weekend hours for pools; seasonal places marked; the proposals of #21 in
  (museums at Śródmieście, Ochota and Stadion, the game museums, Bolimów park, the graduation towers,
  Stadion Znicza, the Służew pond park). Left out of them: Highline in Varso (a viewpoint fits no
  category: the user names one), Fort VIIA (remnants, nothing shows it open to visitors), Znicz's hall
  (no capacity found).

## Work log

- **2026-09-25, #15**: R1 and R3 to their regular termini, zone borders, buses 517, 187, 177, 716, rare
  trains, the hub cards, category Zakupy. Found: the zone border is Falenica, not Michalin; Saturday 26
  September ran a one-off bus timetable, hence the script's typical days.
- **2026-09-25, #16**: category Kino with 29 cinemas (16 new, three split from their hosts), Blue City and
  Reduta, 12 hypermarkets, the canteens, buses 207 and 401, hub cards Włochy and Ochota, `UrsusSuite`. A
  dot on a pressed chip got a ring in the background colour: the slate Kino dot vanished on the ink-coloured
  chip.
- **2026-09-25, #18**: category Rozrywka dropped (see Decisions). Found: #16 had already moved the cinemas
  and six places were left; a browser that remembered the Rozrywka filter opened the page empty, so a
  remembered category the page no longer has is ignored.
- **2026-09-25, #19**: the map. Found: the R2 column joins two trips (see Refreshing); a bus shape can
  end 43 m short of the terminus pole (177 at Os. Górczewska); tiles scaled to a fractional zoom show
  seams in Chromium, so the map keeps whole zoom levels; CARTO tiles now need an API key, OSM's do not.
- **2026-09-25, #20**: buses 194, 220 and 228 from Ursus - Ratusz on the map and the page, 14 places with
  them, and six at existing stops (Park Kombatantów at Włochy, Izba Pamięci at Cm. Wolski, Murall at Os.
  Górczewska, Mini Zoo, Park Linowy and Skalar at Hassa). Left out: the Polish Fiat museum (its domain
  serves a casino, nothing else shows it open), Fort III Blizne (private, a paintball field), Kino
  Akademickie at WAT (no programme found), the Bemowo ice rink (its page did not answer). Teatr IMKA's
  domain is gone; its stage at Kocjana 3 is now Bemowskie Centrum Kultury's Scena Kocjana.
- **2026-09-25, #21**: one or two categories a place (49 have two), PKiN, Łazienki and Nieborów split;
  hypermarkets cut to three a chain (five Kauflands and Galeria Wileńska out), Eurospar in Blue City and
  two Selgros halls in; 22 pools; Torwar and five other concert venues; bus stops Hala Kopińska,
  Wawelska, Rozbrat, Hynka and Dostawcza. Found: Neon Muzeum left Soho Factory for PKiN in 2025; Park
  Skarbków was never a manor park; "Galeria Młodych" in Willa Radogoszcz is classes, not a gallery.
- **2026-09-26, #21**: rebased on #20; Szamoty's walk (Purpose); Aldi (Ożarów, Warszawa Zachodnia, which
  got a card, Karolkowa) and Netto (Kleszczowa, Michałowice, Ożarów); Auchan in Okęcie Park and Wola Park
  with Multikino, Kaufland Fort Wola as the chain's biggest, Plac Unii out as a fourth Carrefour; pools
  Pingwin, CRS Bielany and Albatros, saunas and rare attractions for all; 13 places from the proposals;
  new bus stops Orlich Gniazd, Spisaka, GUS and Bełdan.
  Found: measuring from the edge of Szamoty cut five places 27–32 minutes from its centre, so a new
  measure is checked against a walk the user knows first; Stacja Muzeum is shut 21 September–2 October
  2026; the Aldi at Warszawa Zachodnia (February 2026) is the chain's first at a station and opens on
  Sundays too.
- Not checked so far: GTFS counts against KOLEO or the printed timetable; walking routes other than from
  Szamoty (distances are straight lines).
