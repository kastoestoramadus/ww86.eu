package eu.ww86.site

class SiteSuite extends munit.FunSuite:

  test("the rights notice names the year and reserves text and data mining, in the language of the page") {
    assertEquals(
      Site.rights(Language.English, 2027),
      "© 2027 Waldemar Wosiński. All rights reserved, including text and data mining (Art. 4(3) of Directive (EU) 2019/790). Developed with agentic AI systems."
    )
    assertEquals(
      Site.rights(Language.Polish, 2027),
      "© 2027 Waldemar Wosiński. Wszelkie prawa zastrzeżone, również w zakresie eksploracji tekstów i danych (art. 26³ ustawy o prawie autorskim i prawach pokrewnych). Strona rozwijana z użyciem systemów agentowych."
    )
  }
