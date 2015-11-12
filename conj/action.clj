#(swap! game-state rules/play-card (first (key c)) square-index)
#(swap! game-state rules/fall-back square-index)
#(swap! game-state rules/pass)
