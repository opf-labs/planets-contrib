The Planets Invocation Tool
===========================

This is a command-line-oriented tool specification and invocation system. It aims
to make it easy to describe the preservation actions and the tools used to perform
them, and also make is simple to invoke preservation actions defined by others, 
both locally and over the web.


Wrap pdfbox as jhove2 module.
Include our pw spotter logic etc.
And then blog about how to do it.


Tool Specifications
-------------------

As well as preservation action tools, a tool spec. can also describe a remote 
service registry through which hosted services can be invoked.

* Lightweight tool spec as XML or ant or mojo, speccing command line pattern, optional env etc.
* Or java cp and main class and args. Allows stack analysis.
* ID used in invokation, but binary hash used to spot uniqueness.
* Resist parameter enumeration as this is not really necessary to work out what's going on.
* Pass args as command line or string array, split to string array when analysing.
* i.e. minimal enforced spec in tool spec. Even pathways are only for discovery - can capture usage without them.
* Indeed, how do we use these specs?
* Extend to other cases? Identity or validate etc, by regexing the output?
* Tool spec (.ptspec) may include version.command eg 'file -v'. Response is included verbatim.
* Tool spec may be derived from another explicitly. eg multiple versions of same tool by modifing env.path only.
* Install text, parameter descriptions, in or out formats as optional but recommended.
* If install package then install-dir available as field for env description, eg FITS_HOME or JHove etc.
* Spec can define where additional parameters go, defaults to eol.
* Write specs for kakadu-5, openjpeg and jasper. Automatch, ie default to highest match, so 'pit tr kakadu in out' would work?


### Instrumentation & Measurement ###

* Need a proximal hash for binary signatures. Length of diff of strings? Pull out method signatures and sort and diff those? Really need sig of whole stack, easy on jvm (?) and Linux. 
* JVM hashed jars for uniqueness, enumerated classpath for similarity? Hash classes too for precise similarity? Do dlls have method hashes?
* Hash of input files and outputs too, of course. Again, similarity indexes would be very useful here.
* Inject code into java calls automatically. Fix up proxy, measure performance etc,
* Freak out if the stack changes during a run.
* Bg thread to monitor mem Use over time, especiallly for file set runs.


PIT Actions
-----------

### Tool Spec Management ###

Add a spec to the local register of known tools:
    pit add <file-or-url.ptspec>

Remove a spec from the local register:
    pit [rm|remove] <toolspec-id>

List all known tool specs:
    pit [ls|list]

Check if a tool spec is valid/up to scratch etc:
    pit [tt|tooltest] <toolspec-id>

### Preservation Actions ###

pit [id|identify] <toolspec-id> <file> [<extra parameters>]

pit [val|validate] <toolspec-id> <file> [<extra parameters>]

pit [tr|transform|cv|convert] <toolspec-id> <input> <output> [<extra parameters>]

pit [pf|perform] <toolspec-id> <input>

??? pit [vw|view] <toolspec-id> <input>

When these actions are called, the framework records lots of useful metadata 
about the process and the content. This data is stored in PIT reports (see later).

* Extra pit flags to turn off heavy profiling (total time always included)

### Querying Reports ###

* Export to CSV, etc. 
* Can do some aggregation.


### Sharing Tool Specs and Reports ###

The tool also allows toolspecs and reports to be shared between users via a website.

* Tool specs get username based uris
* Tools and stacks are identified via hashes and stack hash-of-hashes etc
* Env is not included in report or published spec by default, as may be private. Http proxy password? Allow mode with exclusions as env can affect performance.
* Download specs and test?
* Allow test inputs in spec to validate configuration. Can compare with output. Can publish if a licence is given.
* Auto-install mode? Attempt to look at all known tool specs and match up with local sw?
* Easy way to upload to shared space? A servlet that auths against opf drupal? Or open id, or Github backend?
* Pit knows about the OPF tool registry from the start. Tools can include install info, eg mvn or download this package and expand? Do it auto?


### Hosting Preservation Services ###

* Pit serve attempts to push tool spec into a service registry. Requires selftest is in place.

PIT Reports
-----------

XML breakdown of data about presevation action executions.

Includes timing, resource usage, exe digests and possibly the full stacktrace/dependency analysis.

Effective spec is in report.


What do we need from the Planets Testbed?
-----------------------------------------

Testbed testing has two aspects, testing tools do basic ops or testing higher-level ideas?
Does google image format evaluation fit?

Does round trip jp2 testing fit?

If we spec basic ops from tools, we can collect basic data and start to build larger tests from them.

Eg tool spec lets us share data on tool usage. Test spec may be ad hoc but would be focused on outcomes from sets of tool specs.

Eg tool specs used to check reproducible migrations and comparison.

Test suite is different set up that eg runs multiple migrates overs the same data and auto compares the results against eachother or against expectations.

Test results for basic ops etc aggregates and basic visualisation tools too.


