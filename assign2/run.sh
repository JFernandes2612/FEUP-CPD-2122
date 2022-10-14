#!/usr/bin/env bash
#! /bin/bash

cd src

sh ../scripts/build.sh

cd build

gnome-terminal --title "Store1" -- /bin/bash -c "sh ../../scripts/store.sh 224.0.0.0 8080 1 8081" &
gnome-terminal --title "Store2" -- /bin/bash -c "sh ../../scripts/store.sh 224.0.0.0 8080 2 8082" &
gnome-terminal --title "Store3" -- /bin/bash -c "sh ../../scripts/store.sh 224.0.0.0 8080 3 8083"
