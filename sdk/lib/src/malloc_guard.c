#include <errno.h>
#include <stddef.h>

// Guard malloc/calloc/realloc against requests that exceed the total heap
// capacity. Without this, dlmalloc's last_remainder code path can return a
// non-NULL pointer for huge requests without calling sbrk at all.
//
// Compiled into each executable via fp_add_outputs (not into libfp-sdk.a), so
// libfp-sdk.a has no dependency on --wrap linker flags.

static inline size_t heap_size(void) {
    extern char __sheap;
    extern char __eheap;
    return (size_t)(&__eheap - &__sheap);
}

extern void *__real_malloc(size_t);
void *__wrap_malloc(size_t size) {
    if (size > heap_size()) { errno = ENOMEM; return NULL; }
    return __real_malloc(size);
}

extern void *__real_calloc(size_t, size_t);
void *__wrap_calloc(size_t nmemb, size_t size) {
    // shortcut: rejects nmemb*size > heap even when fragmentation would prevent it sooner
    if (size != 0 && nmemb > heap_size() / size) { errno = ENOMEM; return NULL; }
    return __real_calloc(nmemb, size);
}

extern void *__real_realloc(void *, size_t);
void *__wrap_realloc(void *ptr, size_t size) {
    if (size > heap_size()) { errno = ENOMEM; return NULL; }
    return __real_realloc(ptr, size);
}
