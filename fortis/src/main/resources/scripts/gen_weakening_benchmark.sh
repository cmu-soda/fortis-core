#!/bin/bash

# [](a && c -> b), variables 3:11:4, trace size 5:40:5, number of traces 5:100:5
for y in $(seq 5 5 40); do
  for x in $(seq 5 5 100); do
    java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c" -o "[](a -> b)" -e "[](a && c -> b)" -S "$y" -P "$x" -N "$x" -D > w_antec_3_"$y"_"$x"_"$x".trace
  done

  for x in $(seq 5 5 100); do
    java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c,d,e,f,g" -o "[](a -> b)" -e "[](a && c -> b)" -S "$y" -P "$x" -N "$x" -D > w_antec_7_"$y"_"$x"_"$x".trace
  done

  for x in $(seq 5 5 100); do
    java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c,d,e,f,g,h,i,j,k" -o "[](a -> b)" -e "[](a && c -> b)" -S "$y" -P "$x" -N "$x" -D > w_antec_11_"$y"_"$x"_"$x".trace
  done
done

# [](a -> b || c), variables 3:11:4, trace size 5:40:5, number of traces 5:100:5
for y in $(seq 5 5 40); do
  for x in $(seq 5 5 100); do
    java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c" -o "[](a -> b)" -e "[](a -> b || c)" -S "$y" -P "$x" -N "$x" -D > w_conseq_3_"$y"_"$x"_"$x".trace
  done

  for x in $(seq 5 5 100); do
    java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c,d,e,f,g" -o "[](a -> b)" -e "[](a -> b || c)" -S "$y" -P "$x" -N "$x" -D > w_conseq_7_"$y"_"$x"_"$x".trace
  done

  for x in $(seq 5 5 100); do
    java -cp ../../bin/fortis.jar cmu.s3d.fortis.weakening.benchmark.BenchmarkKt -l "a,b,c,d,e,f,g,h,i,j,k" -o "[](a -> b)" -e "[](a -> b || c)" -S "$y" -P "$x" -N "$x" -D > w_conseq_11_"$y"_"$x"_"$x".trace
  done
done
