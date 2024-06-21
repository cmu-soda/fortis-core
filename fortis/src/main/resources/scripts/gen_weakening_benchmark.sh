#!/bin/bash

# [](a && c -> b), variables 3:11:4, trace size 5, number of traces 5:100:5
for x in $(seq 5 5 100); do
  java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c" -o "[](a -> b)" -e "[](a && c -> b)" -S 5 -P "$x" -N "$x" -D > w_antec_3_5_"$x"_"$x".trace
done

for x in $(seq 5 5 100); do
  java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c,d,e,f,g" -o "[](a -> b)" -e "[](a && c -> b)" -S 5 -P "$x" -N "$x" -D > w_antec_7_5_"$x"_"$x".trace
done

for x in $(seq 5 5 100); do
  java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c,d,e,f,g,h,i,j,k" -o "[](a -> b)" -e "[](a && c -> b)" -S 5 -P "$x" -N "$x" -D > w_antec_11_5_"$x"_"$x".trace
done

# [](a && c -> b), variables 3:11:4, trace size 10:100:5, number of traces 5
for x in $(seq 10 5 100); do
  java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c" -o "[](a -> b)" -e "[](a && c -> b)" -S "$x" -P 5 -N 5 -D > w_antec_3_"$x"_5_5.trace
done

for x in $(seq 10 5 100); do
  java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c,d,e,f,g" -o "[](a -> b)" -e "[](a && c -> b)" -S "$x" -P 5 -N 5 -D > w_antec_7_"$x"_5_5.trace
done

for x in $(seq 10 5 100); do
  java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c,d,e,f,g,h,i,j,k" -o "[](a -> b)" -e "[](a && c -> b)" -S "$x" -P 5 -N 5 -D > w_antec_11_"$x"_5_5.trace
done

# [](a -> b || c), variables 3:11:4, trace size 5, number of traces 5:100:5
for x in $(seq 5 5 100); do
  java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c" -o "[](a -> b)" -e "[](a -> b || c)" -S 5 -P "$x" -N "$x" -D > w_conseq_3_5_"$x"_"$x".trace
done

for x in $(seq 5 5 100); do
  java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c,d,e,f,g" -o "[](a -> b)" -e "[](a -> b || c)" -S 5 -P "$x" -N "$x" -D > w_conseq_7_5_"$x"_"$x".trace
done

for x in $(seq 5 5 100); do
  java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c,d,e,f,g,h,i,j,k" -o "[](a -> b)" -e "[](a -> b || c)" -S 5 -P "$x" -N "$x" -D > w_conseq_11_5_"$x"_"$x".trace
done

# [](a -> b || c), variables 3:11:4, trace size 10:100:5, number of traces 5
for x in $(seq 10 5 100); do
  java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c" -o "[](a -> b)" -e "[](a -> b || c)" -S "$x" -P 5 -N 5 -D > w_conseq_3_"$x"_5_5.trace
done

for x in $(seq 10 5 100); do
  java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c,d,e,f,g" -o "[](a -> b)" -e "[](a -> b || c)" -S "$x" -P 5 -N 5 -D > w_conseq_7_"$x"_5_5.trace
done

for x in $(seq 10 5 100); do
  java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c,d,e,f,g,h,i,j,k" -o "[](a -> b)" -e "[](a -> b || c)" -S "$x" -P 5 -N 5 -D > w_conseq_11_"$x"_5_5.trace
done