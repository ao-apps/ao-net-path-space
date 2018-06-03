# [<img src="ao-logo.png" alt="AO Logo" width="35" height="40">](https://aoindustries.com/) [AO Net Path Space](https://aoindustries.com/ao-net-path-space/)
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
	<a href="https://docs.oracle.com/javase/6/docs/api/">
		<img src="https://aoindustries.com/ao-badges/java-6.svg" alt="java: &gt;= 6" />
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
* [Maven Central Repository](https://search.maven.org/#search%7Cgav%7C1%7Cg:%22com.aoindustries%22%20AND%20a:%22ao-net-path-space%22)
* [GitHub](https://github.com/aoindustries/ao-net-path-space)

## Features
* Identifies conflicting spaces.
* Very fast lookups even when managing a large number of spaces.
* Supports `/path` for a single resource.
* Supports `/path/` for a single resource.
* Supports `/path/&ast;` bounded wildcard spaces (matches files in one directory only).
* Supports `/path/&ast;/&ast;` multi-level bounded wildcard spaces (matches files at the given directory depth only).
* Supports `/path/&ast;&ast;` multi-level unbounded wildcard spaces (matches all files and directories within a path, allows other sub-spaces to be allocated).
* Supports `/path/&ast;/&ast;&ast;` multi-level unbounded wildcard within a bounded wildcard (matches all files and directories below the given directory depth, allows other sub-spaces to be allocated).
* Supports `/path/&ast;&ast;&ast;` multi-level greedy wildcard spaces (matches all files and directories within a path, while not allowing any sub-spaces to be allocated).
* Supports `/path/*/***` multi-level greedy wildcard spaces within a bounded wildcard (matches all files and directories below the given directory depth, while not allowing any sub-spaces to be allocated).
* Small footprint, minimal dependencies - not part of a big monolithic package.
* Java 1.6 implementation:
    * Android compatible.
    * Java EE 6+ compatible.

## Motivation
Managing the URL path space is central to web application development.  When components are developed independently and combined into a single URL space, as well as underlying implementation space (such as Java Servlet paths), it is important that each component occupy a well-defined space without unexpected overlapping of other components.  The goal is to reliably identify conflicts within an application's URL or implementation space instead of having unintended overlapping components or access rules.

[SemanticCMS Core Controller](https://semanticcms.com/core/controller/) uses this project to manage which requests are passed along for direct processing by the local Servlet container.  This layer of allocating the servlet space is important because resources from `/META-INF/resources/` within all project JAR files are merged into a single space with the potential to inadvertently overlap.  Explicit activation of servlet space through the controller helps identify these conflicts early in the module development or integration phase.

## Evaluated Alternatives
No alternative known.  Please let us know if this effort is redundant.  Always happy for any assistance in curing ignorance.

## Contact Us
For questions or support, please [contact us](https://aoindustries.com/contact):

Email: [support@aoindustries.com](mailto:support@aoindustries.com)  
Phone: [1-800-519-9541](tel:1-800-519-9541)  
Phone: [+1-251-607-9556](tel:+1-251-607-9556)  
Web: https://aoindustries.com/contact
