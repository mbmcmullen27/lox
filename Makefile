objects := $(patsubst %.c,%.o,$(wildcard src/*.c))
.PHONY: clean

clox: $(objects)
	clang -o clox $(objects)
src/%.o: %.c #$$(grep -o '"[^"]*\.h"' src/%.c)
	clang -c src/%.c -o src/%.o
clean: 
	rm src/*.o clox