package eu.ww86.web

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

/** One script for the whole site: every widget mounts itself only if its container is on the page. */
@main def main(): Unit =
  documentEvents(_.onDomContentLoaded).foreach { _ =>
    mount("digits-widget", DigitsWidget())
  }(unsafeWindowOwner)

private def mount(containerId: String, widget: => HtmlElement): Unit =
  Option(dom.document.getElementById(containerId)).foreach { container =>
    container.innerHTML = ""
    render(container, widget)
  }
