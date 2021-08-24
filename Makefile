clox: src/main.o src/memory.o src/chunk.o src/debug.o src/value.o
	clang src/main.o src/memory.o src/chunk.o src/debug.o src/value.o -o clox
src/main.o: src/main.c src/common.h src/chunk.h src/debug.h
	clang -c src/main.c -o src/main.o
src/memory.o: src/memory.c src/memory.h
	clang -c src/memory.c -o src/memory.o
src/chunk.o: src/chunk.c src/chunk.h src/memory.h
	clang -c src/chunk.c -o src/chunk.o
src/debug.o: src/debug.c src/debug.h
	clang -c src/debug.c -o src/debug.o
src/value.o: src/value.c src/value.h
	clang -c src/value.c -o src/value.o
clean: src/*
	rm src/*.o clox