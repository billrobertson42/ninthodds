# ninthodds

A screen to compute and visualize combat odds for [The Ninth Age](https://the-ninth-age.com).

![Screenshot](https://raw.githubusercontent.com/billrobertson42/ninthodds/master/screenshot.png)

## Overview

This is a Clojurescript/Reagent application that computes combat odds
for version two of [The Ninth Age](https://the-ninth-age.com).

This version has reached minimum viable product (MVP) status. It can
compute basic combat odds and factor in armor and special saves.

For example, given 10 attacks with equal weapon skill, and identical
strength and resilience, What are the odds that 2 or 3 wounds will be
caused?

## Current Features

The program can do the following...

* Allows you to enter number of attacks (n)
* Allows you to enter offensive and defensive stats
* Computes the odds of causing 0-n wounds and displays that in a graph
* Saves are considered in the wound calculation
* Has internal logic for rerolls
* Has internal logic for poison attacks
* Reasonable level of responsiveness (i.e. displays OK on computer or phone)

## MVP Features (todo)

* Provide user interface elements to activate poison and reroll logic
* Wordsmith the UI a bit

## Future Features

* Figure out additional feathres to make this a good tool for evaluating unit effectiveness
* Current model is based on binomial distribution -- fine for fixed number of trials, how to expand?
* Add shooting

### Using

Once this has reached MVP status, I will host it somewhere so you can just try it out.

### Hacking

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2017 Bill Robertson

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
