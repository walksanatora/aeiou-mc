#!/usr/bin/env bash

echo "Note: this script will install dectalk and SAM"
echo "to $HOME/.tts"
echo "this script also requires that git and gcc be on PATH"
read

if ! type git; then
  echo "Missing git"
  exit 1
fi
if ! type gcc; then
  echo "Missing gcc"
  exit 1
fi

mkdir -p $HOME/tts
## no longer needed as i compiled it to WASM
#git clone https://github.com/walksanatora/SAM
#cd SAM
#make -j
#cp target/c/sam-inline $HOME/.tts
#cd ..
#rm -rf SAM

git clone https://github.com/dectalk/dectalk
cd dectalk/src
./configure --prefix=$HOME/.tts/dtalk --disable-audio
make install -j
ln -s $HOME/.tts/dtalk/say $HOME/tts/dectalk
cd ../..
rm -rf dectalk

