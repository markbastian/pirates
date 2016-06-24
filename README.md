# Pirates: A Clojure implementation of the Cartagena board game.

This was my first non-trivial Clojure project. It's an implementation of the [Cartagena board 
game](http://boardgamegeek.com/boardgame/826/cartagena) in Clojure. The rules are all found in rules.cljx. Cljx was used
since at some point I'd like to make a web gui as well. The program can be launched from swingui.clj. I apologize up
front for the programmer art. The main goal was to explore how a desktop app could be written in Clojure. Coming from 
the Java and Scala worlds, I was quite impressed by how concise the application is. The majority of the code is Swing,
and that could probably be reduced a lot using seesaw or similar.

## Installation

From a repl, uncomment and run the frame method. Alternatively, lein run does the trick. You can also bundle the app
with lein compile, lein uberjar.

## Usage

Double-click the executable jar. To take a turn, right click on the square with your colored dot (pirate) and select an
action. Once you have all of your pirates in the boat, you win. You are assigned a color (pick the one you want that
pops up) and when your cards (icons on the right) are your color, it is your turn. It's a pretty minimal UI :(

One thing I've noticed is that the initial UI may not display correctly on Windows. If you resize the frame it will render correctly. I've only got a Mac, so can't easily debug.

## Design

This app was written as more of an exercise in learning Clojure than in writing a fun game (that takes a lot of work),
but the cool pattern I've used since is 1) creating business logic in pure functional form, 2) creating a stateful UI in
whatever framework you want, and 3) using an atom, agent, or ref to manage a state instance that bridges the UI and the 
logic. Pretty cool!

## Contributors

If anyone out there wants to add AI, better art, online multiplayer, etc. I'd be more than happy to accept input or collaborate.

## License

Copyright © 2015 Mark Bastian

Distributed under the Eclipse Public License, the same as Clojure.

## Licenses for Art

Designed by Freepik
<div>Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a> is licensed under <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0">CC BY 3.0</a></div>
