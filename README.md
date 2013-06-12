Summary Lapse+ Description
==========================

Introduction
------------

LAPSE+ is a security scanner, based on the static analysis of code, for detecting vulnerabilities in Java EE Applications. It is implemented as a plugin for Eclipse IDE, the well-known environment for developing Java applications.

LAPSE+ is based on the GPL software LAPSE, developed by SUIF Compiler Group of Stanford University. The latest stable release by Stanford is LAPSE 2.5.6, dating from 2006, being this release obsolete in terms of the number of vulnerabilities detected and its integration with new versions of Eclipse.

Therefore, LAPSE+ is an enhanced version of LAPSE, updated to work with Eclipse Helios, providing a wider catalog of vulnerabilities and improvements in the analysis of code.

Overview
--------

The vulnerabilities detected by LAPSE+ are related to the injection of untrusted data to manipulate the behavior of the application. These type of vulnerabilities are defined by OWASP as the most common vulnerabilities in web applications.

The detection of these kind of vulnerabilities is performed in three steps:

1. *Vulnerability Source.* First, the tool detects the points of code that can be source of an attack of malicious data injection.
2. *Vulnerability Sink.* After detecting the points of code that can be target of data injection, LAPSE+ identifies the points that can propagate the attack and manipulate the behaviour of the application.
3. *Provenance Tracker.* Finally, we check if we can reach a Vulnerability Source from a Vulnerability Sink. It this occurs, we have a vulnerability in our code.

LAPSE+ Views
------------

LAPSE+ plugin provides three different views for the analysis of vulnerabilities. The first two views show the vulnerability sources and sinks detected, respectively, indicating their category, identifying the line of code and the exact sentence where they occur. The Provenance Tracker View shows the backward propagation tree from a vulnerability sink.

**Vulnerability Sources View**
This view shows the points of code that can be source of untrusted data injection.

![Vulnerability Sources View](http://img200.imageshack.us/img200/6356/sourcesf.png "Vulnerability Sources View")

**Vulnerability Sinks View**
This view shows the points of code that can insert the untrusted data in the application, manipulating its behaviour.

![Vulnerability Sinks View](http://img19.imageshack.us/img19/3266/sinks.png "Vulnerability Sinks View")

**Provenance Tracker View**
This view traces the backward propagation tree from a vulnerability sink in order to check if it reaches a vulnerability source. If this happens we hava a vulnerability in our code.

![Provenance Tracker View](http://img155.imageshack.us/img155/6168/propagation1.png "Provenance Tracker View")


*Vulnerabilities Detected by LAPSE*

LAPSE+ identifies the following Java EE Application vulnerability sources and sinks:

***Sources***

  * Parameter Tampering.
  * Header Manipulation.
  * URL Tampering.
  * Cookie Poisoning.
  * Information Leakage.

***Sinks***
 
  * SQL Injection.
  * Cross-site Scripting (XSS).
  * Path Traversal.
  * Command Injection.
  * Information Leakage.
  * XPath Injection.
  * XML Injection.
  * LDAP Injection.

Requirements
------------

  * Eclipse 3.2 (Helios).
  * Java 1.6
