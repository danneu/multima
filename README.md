multima
=======

A modest Clojure MUD

Learning from [jcromartie/dunjeon](https://github.com/jcromartie/dunjeon) until I (hopefully) get on my own two feet.

## Progress

Terminal 1: Start server

    $ lein run 
    
Terminal 2: Connect client

    $ telnet localhost 5000

    Welcome to Multima.
    Players: 1

    You awake in a chamber.
    > look
    Your chamber includes a bed and a small round window that gazes out into space. Canned sardines have more room than this.
     - down: tube

    > go down
    Heading :down
    You're squeezed into a tube.
     - up: chamber

    > go up
    Heading :up
    Your chamber includes a bed and a small round window that gazes out into space. Canned sardines have more room than this.
     - down: tube

    > lol
    What?

    > quit
    Quitting...
    Connection closed by foreign host.
