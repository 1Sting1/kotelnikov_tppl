#!/bin/bash
SDK_PATH=$(xcrun -sdk macosx --show-sdk-path)
nasm -f macho64 main.asm. -o main.o
ld -macos_version_min 11.0 -lSystem -syslibroot "$SDK_PATH" -o main main.o
./main
rm main.o