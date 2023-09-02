#!/usr/bin/env bash

if ! type git; then
  echo "Missing git"
  exit 1
fi
if ! type gcc; then
  echo "Missing gcc"
  exit 1
fi

git clone https://github.com/walksanatora/SAM
cd SAM
make -j
cp target/c/sam-inline /usr/bin/sam-inline
cd ..
rm -rf SAM

git clone https://github.com/dectalk/dectalk
cd dectalk/src
./configure
make -j
make install -j
mv /usr/bin/say /usr/bin/dectalk
cd ../..
rm -rf dectalk

