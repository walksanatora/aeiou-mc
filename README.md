# Aeiou MC

# Setup

## Client Setup
on the client... just install this mod, the server does most of the heavy lifting (unless you want to use in singleplayer)
if you want to use in SP you will have to follow the server setup guide

## Server Setup
since this TTS mod is "module" based it will only load the SAM/dectalk modules if the required programs are on path
you can compile dectalk from [https://github.com/dectalk/dectalk](https://github.com/dectalk/dectalk)
and you need to compile sam-inline from my fork of sam [https://github.com/walksanatora/SAM](https://github.com/walksanatora/SAM)
and put them on path.
if you want to make addon all you have to do is implement TTSEngine and add a factory function to `AeiouMod.engines`
