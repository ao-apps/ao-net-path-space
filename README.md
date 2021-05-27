# [<img src="ao-logo.png" alt="AO Logo" width="35" height="40">](https://github.com/aoindustries) [AO Net Path Space](https://github.com/aoindustries/ao-net-path-space)
<p>
	<a href="https://aoindustries.com/life-cycle#project-alpha">
		<img src="https://aoindustries.com/ao-badges/project-alpha.svg" alt="project: alpha" />
	</a>
	<a href="https://aoindustries.com/life-cycle#management-preview">
		<img src="https://aoindustries.com/ao-badges/management-preview.svg" alt="management: preview" />
	</a>
	<a href="https://aoindustries.com/life-cycle#packaging-developmental">
		<img src="https://aoindustries.com/ao-badges/packaging-developmental.svg" alt="packaging: developmental" />
	</a>
	<br />
	<a href="https://docs.oracle.com/en/java/javase/11/docs/api/">
		<img src="https://aoindustries.com/ao-badges/java-11.svg" alt="java: &gt;= 11" />
	</a>
	<a href="http://semver.org/spec/v2.0.0.html">
		<img src="https://aoindustries.com/ao-badges/semver-2.0.0.svg" alt="semantic versioning: 2.0.0" />
	</a>
	<a href="https://www.gnu.org/licenses/lgpl-3.0">
		<img src="https://aoindustries.com/ao-badges/license-lgpl-3.0.svg" alt="license: LGPL v3" />
	</a>
</p>

Manages allocation of a path space between components.

## Project Links
* [Project Home](https://aoindustries.com/ao-net-path-space/)
* [Changelog](https://aoindustries.com/ao-net-path-space/changelog)
* [API Docs](https://aoindustries.com/ao-net-path-space/apidocs/)
* [Maven Central Repository](https://search.maven.org/artifact/com.aoindustries/ao-net-path-space)
* [GitHub](https://github.com/aoindustries/ao-net-path-space)

## Features
* Identifies conflicting spaces.
* Very fast lookups even when managing a large number of spaces.
* Supports several types of spaces:
    * Wildcard spaces:
        * `/path/*` - wildcard space - matches all resources in one path depth only.
        * `/path/*/*` - multi-level wildcard space - matches all resources at the given path depth only.
        * `/path/*/*/*` - any number of levels allowed.
    * Unbounded spaces - Allows other sub-spaces to be allocated:
        * `/path/**` - unbounded space - matches all resources at or below one path depth.
        * `/path/*/**` - multi-level unbounded space - matches all resources at or below the given path depth.
        * `/path/*/*/**` - any number of levels allowed.
    * Greedy spaces - Like unbounded spaces, but do not allow other sub-spaces to be allocated:
        * `/path/***` - greedy space - matches all resources at or below one path depth.
        * `/path/*/***` - multi-level greedy space - matches all resources at or below the given path depth.
        * `/path/*/*/***` - any number of levels allowed.
* Small footprint, minimal dependencies - not part of a big monolithic package.
* Java 1.8 implementation:
    * Android compatible.

## Motivation
Managing the URL path space is central to web application development.  When components are developed independently and combined into a single URL space, as well as underlying implementation space (such as Java Servlet paths), it is important that each component occupy a well-defined space without unexpectedly overlapping other components.  The goal is to reliably identify conflicts within an application's URL or implementation space instead of unintentionally having overlapping components or access rules.

[SemanticCMS Core Controller](https://github.com/aoindustries/semanticcms-core-controller) uses this project to manage which requests are passed along for direct processing by the local Servlet container.  This layer of allocating the servlet space is important because resources from `/META-INF/resources/` within all project JAR files are merged into a single space with the potential to inadvertently overlap.  Explicit reservation of servlet space through the controller helps identify these conflicts early in the module development and integration phases.

## Evaluated Alternatives
No alternative known.  Please let us know if this effort is redundant.  Always happy for any assistance in curing ignorance.

## Contact Us
For questions or support, please [contact us](https://aoindustries.com/contact):

Email: [support@aoindustries.com](mailto:support@aoindustries.com)  
Phone: [1-800-519-9541](tel:1-800-519-9541)  
Phone: [+1-251-607-9556](tel:+1-251-607-9556)  
Web: https://aoindustries.com/contact
