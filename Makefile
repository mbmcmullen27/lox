objects := $(patsubst %.c,%.o,$(wildcard src/*.c))

clox: $(objects)
	clang -o clox $(objects)
src/%.o: %.c $$(grep -o '"[^"]*\.h"' src/%.c)
	echo -e "$$(grep -o '"[^"]*\.h"' src/%.c)"
	clang -c src/%.c -o src/%.o
# src/main.o: src/main.c src/common.h src/chunk.h src/debug.h src/vm.h
# 	clang -c src/main.c -o src/main.o
# src/memory.o: src/memory.c src/memory.h
# 	clang -c src/memory.c -o src/memory.o
# src/chunk.o: src/chunk.c src/chunk.h src/memory.h
# 	clang -c src/chunk.c -o src/chunk.o
# src/debug.o: src/debug.c src/debug.h
# 	clang -c src/debug.c -o src/debug.o
# src/value.o: src/value.c src/value.h
# 	clang -c src/value.c -o src/value.o
# src/vm.o: src/vm.c src/vm.h
# 	clang -c src/vm.c -o src/vm.o
# src/scanner.o: src/scanner.c src/scanner.h
# 	clang -c src/scanner.c -o src/scanner.o
# src/compiler.o: src/compiler.c src/compiler.h src/scanner.h src/common.h
# 	clang -c src/compiler.c -o src/compiler.o
clean: src/*
	rm src/*.o clox