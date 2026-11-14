set(TESTNAME malloc)

add_executable(${TESTNAME} ${TESTNAME}.c)
include(${CMAKE_SOURCE_DIR}/cmake/fp-app.cmake)

target_link_libraries(${TESTNAME} fp-sdk)

# Without this, GCC's builtin malloc attribute lets it assume malloc never
# returns NULL, so it compiles fp_assert(massive == NULL) as an unconditional
# abort without ever calling malloc(0x30000000).
target_compile_options(${TESTNAME} PRIVATE -fno-builtin-malloc)

fp_add_outputs(${TESTNAME})
fp_add_test_runs_to_completion(${TESTNAME})
