#!/usr/bin/env bash

echo "Note: this script will install dectalk and SAM"
echo "to $HOME/.tts"
echo "this script also requires that git and gcc be on PATH"
echo "press enter to continue"
read

if ! type git; then
  echo "Missing git"
  exit 1
fi
if ! type gcc; then
  echo "Missing gcc"
  exit 1
fi

mkdir -p $HOME/.tts
cd $HOME/.tts
pwd

git clone https://github.com/walksanatora/SAM
cd SAM
make -j
cp target/c/sam-inline $HOME/.tts
cd ..
#rm -rf SAM

git clone https://github.com/dectalk/dectalk dectalk-src
cd dectalk-src/src
./configure --prefix=$HOME/.tts/dtalk --disable-audio
pwd
make install -j
ln -s $HOME/.tts/dtalk/say $HOME/.tts/dectalk
cd ../..
#rm -rf dectalk

