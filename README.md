# Aeiou MC

when first joining a MC server/singleplayer world it *will* give you a random voice which you may want to configure using `/tts cfg`<br>
<br>
run `/tts help` to get all commands

# Setup

## Client Setup
on the client... just install this mod, the server does most of the heavy lifting (unless you want to use in singleplayer)<br>
if you want to use in SP you will have to follow the server setup guide<br>

## Server Setup
since this TTS mod is "module" based it will only load the SAM/dectalk modules if the required programs are on path<br>
you can compile dectalk from [https://github.com/dectalk/dectalk](https://github.com/dectalk/dectalk)<br>
and you need to compile sam-inline from my fork of sam [https://github.com/walksanatora/SAM](https://github.com/walksanatora/SAM)<br>
and put them on path. (or System.getProperty("user.home")+"/.tts")<br>
<br>
On linux systems (assuming you have git and gcc) you can run `setup.sh` from this repo.<br>
it will automatically download and install these programs to $HOME/.tts

# Addons
if you want to make addon all you have to do is implement TTSEngine and add a factory function to `AeiouMod.engines`<br>
