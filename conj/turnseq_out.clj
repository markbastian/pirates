(create-turn-sequence #{:red :green :blue :brown})
=> {:green :red, :red :blue, :blue :brown, :brown :green}
(create-turn-sequence (shuffle #{:red :green :blue :brown}))
=> {:red :green, :green :blue, :blue :brown, :brown :red}
(create-turn-sequence [:yellow :purple])
=> {:yellow :purple, :purple :yellow}
