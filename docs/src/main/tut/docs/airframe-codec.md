---
layout: docs
title: airframe-codec
---
# Airframe Codec

airframe-codec is an [MessagePack](https://msgpack.org)-based data encoder + decoder.

With airframe-codec you can:
- Encode Scala objects (e.g., case classes, collection, etc.) into MessagePack format, and decode it. Object serialization/deserialization.
- Convert JDBC result sets into MessagePack
- Add you custom codec (implementing pack/unpack)
- You can use airframe-tablet is for reading CSV/TSV/JSON/JDBC data etc.    

airframe-codec supports schema-on-read data conversion.
For example, even if your data is string representation of integer values, e.g., "1", "2, "3", ..., 
airframe-codec can convert it into integers if the target schema (e.g., objects) requires integer values. 

## Usage

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.wvlet.airframe/airframe-codec_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.wvlet.airframe/airframe-codec_2.12/)

```scala
libraryDependencies += "org.wvlet.airframe" %% "airframe-codec" % "(version)"
```
