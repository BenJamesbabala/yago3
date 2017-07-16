# YAGO


YAGO is a large semantic knowledge base, derived from Wikipedia, WordNet, WikiData, GeoNames, and other data sources. Currently, YAGO knows more than 17 million entities (like persons, organizations, cities, etc.) and contains more than 150 million facts about these entities.

YAGO is special in several ways:

  * The accuracy of YAGO has been manually evaluated, proving a confirmed accuracy of 95% (*). Every relation is annotated with its confidence value.
  * YAGO combines the clean taxonomy of WordNet with the richness of the Wikipedia category system, assigning the entities to more than 350,000 classes.
  * YAGO is anchored in time and space. YAGO attaches a temporal dimension and a spatial dimension to many of its facts and entities.
  * In addition to a taxonomy, YAGO has thematic domains such as "music" or "science" from WordNet Domains.
  * YAGO extracts and combines entities and facts from 10 Wikipedias in different languages.

YAGO is at jointly developed at the [DBWeb group](http://dbweb.enst.fr/) at [Télécom ParisTech University](https://www.telecom-paristech.fr/), the [Databases and Information Systems group](http://www.mpi-inf.mpg.de/departments/databases-and-information-systems/) at the [Max Planck Institute for Informatics](http://www.mpi-inf.mpg.de/home/), and [Ambiverse](https://www.ambiverse.com/).

<!-- TODO: mailing list -->

(*) Not every version of YAGO is manually evaluated. Most notably, the version generated by this code may not be the one that we evaluated!  Check the versions on the [YAGO download page](http://yago-knowledge.org)

# YAGO Code Repository

### Target audience

If you are just interested in the data of YAGO, there is no need to use the present code repository. You can download data of YAGO from the [YAGO homepage](http://yago-knowledge.org).

If you are interested in using the source code of YAGO, or in contributing to it, read on. The source code of YAGO is a Java project that extracts facts from Wikipedia and the other data sources, and stores these facts in files. These files make up the YAGO knowledge base.

If you run the code yourself, you can define (a) what Wikipedia languages to cover, and (b) which specific [Wikipedia](https://www.wikipedia.org/), [Wikidata](https://www.wikidata.org), and [Wikimedia Commons](https://commons.wikimedia.org) snapshots should be used during the build.

### Project components

The following Java projects belong to YAGO
  * Javatools:  These classes are Java utilities. They are shared with other projects.
  * Basics: These classes are used to represent facts, TSV files, etc.
  The files in "data" describe the schema of YAGO.
  * YAGO: This project contains 
    - all main YAGO extractors
    - some hand-crafted data
    - scripts that run YAGO 
  
### Prerequisites

To run YAGO, you need the following:
  * Java 8
  * Maven
  * for the automated downloading of data resources:
    * Python 2.7
    * the Python module `requests` (you can use `pip install requests` to install this module)
    * a unix machine
  * a machine with at least 256 GB of RAM and 1 TB of disk space
  
### The YAGO configuration file

YAGO is configured with a configuration file. Use this [template](blob/master/configuration/yago.ini) to generate your own copy of that file. It should contain the following lines:

  * `reuse = true|false`: Specifies whether a new run of YAGO should overwrite or re-use the facts that have already been generated in a previous run.
  * `yagoFolder = ...`: Specifies the folder where the YAGO facts shall be stored.
  * `languages = en, de, fr, nl, it, es, ro, pl, ar, fa`: Specifies the Wikipedia languages from which YAGO shall extract the facts. Use [ISO 639-1 language codes](https://www.loc.gov/standards/iso639-2/php/code_list.php).
  * `extractors`: List of extractors to run. By default, just use the list from the template.

### Downloading the data sources

YAGO needs the following data sources: 
  * [Wikipedia](https://www.wikipedia.org/): the [latest version](https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2) of `pages-articles`.
  * [Wikidata](https://www.wikidata.org): the latest [version](https://dumps.wikimedia.org/wikidatawiki/entities/20170612/wikidata-20170612-all-BETA.ttl.bz2) of `wikidata-DATE-all-ttl`.
  * [Wikimedia Commons](https://commons.wikimedia.org): the [latest version(https://dumps.wikimedia.org/commonswiki/latest/commonswiki-latest-pages-articles.xml.bz2) of pages-articles.
  * [Geonames](http://www.geonames.org/): the files 
    [countryInfo](http://download.geonames.org/export/dump/countryInfo.txt), 
    [hierarchy](http://download.geonames.org/export/dump/hierarchy.zip),
    [alternateNames](http://download.geonames.org/export/dump/alternateNames.zip), 
    [userTags](http://download.geonames.org/export/dump/userTags.zip), 
    [featureCodes_en](http://download.geonames.org/export/dump/featureCodes_en.txt), 
    [allCountries](http://download.geonames.org/export/dump/allCountries.zip)
  
If you want to download the latest versions of the data sources automatically, add the following line to your YAGO configuration file:

  * `dumpsFolder = ...`: points to a folder where the data sources live.

Then run the following code (works only on a Linux machine):

```bash
python scripts/dumps/downloadDumps.py -i <PATH_TO_YAGO_DUMP_FOLDER> -y <PATH_TO_CONFIGURATION_FILE>
```

This code will create a new configuration file, which you will have to use in the sequel.
  
Alternatively, you can download the required data sources manually. Then add the following lines to your configuration file:

  * `wikipedias = ...`: a comma-separated list of the Wikipedia dumps, in the order of the languages specified with the `languages` parameter.
  * `wikidata = ...`: Points to the WikiData file.
  * `commons_wiki = ...`: Points to the WikiCommons file.
  * `geonames = ...`: Points to the folder where Geonames is stored.


### Running YAGO

Once the configuration file has been prepared and all required resources have been downloaded, a YAGO build can be started like this:

```bash
cd <PATH_TO_YAGO3>
export MAVEN_OPTS=-Xmx220G
mvn clean verify exec:java -Dexec.args=<PATH_TO_CONFIGURATION_FILE>
```

Watch out to use the new configuration file if you used the Python script to download the data resources. Allocating 220G of main memory to YAGO is a reasonable estimate which typically works fine, but of course this highly depends on the number of languages you execute the build for. Increase this value if necessary.

Once the processing finished, all output can be found in the directory given by the `yagoFolder` parameter in your configuration file.

# Code Architecture 

The overall goal of the YAGO architecture is to enable cooperation of several contributors, facilitate debugging and maintenance, and allow users to download only particular pieces of YAGO ("YAGO a la carte").  In short: YAGO is modular, both in code and in data.

The current architecture pursues the goal of modularity at the expense of longer running times and inefficiency.  The rationale is that we do not care if the extraction runs a few hours longer, if we can save a few hours of human work in return.

### Themes

The YAGO data is split into "themes". Each theme corresponds to a file on disk. A theme contains facts (either in RDF or in TSV, see the section on data formats below). Themes can overlap, but should not. The `class basics.Theme` implements a theme.

Themes that are free of duplicates and ready for export are called "final themes". These live in the same folder as the other themes, but start with `yago...`.  The final themes make up the YAGO knowledge base.


### Extractors

An extractor is a unit of Java code that takes as input (1) one or more themes and/or (2) a raw data file, and that produces as output one or more themes.

Extractors implement `extractors.Extractor`. Common postprocessing steps (such as translating entities) implement the class `FollowUpExtractor`. This defines a dependency graph of extractors. Extractors are scheduled in the right order and called by `main.ParallelCaller`.

### Techniques 

Facts can have a meta-fact `extractionSource`. This meta-fact can have a meta-fact `extractionTechnique`. There should be a finite set of techniques that does not grow with the data.

Facts that do not have such an annotation are assumed to be trivially clean.

### Packages

Extractors are split into the following packages:

  * deduplicators: extractors aggregating results from previous ones, removing duplicate facts
  * extractors: abstract classes specifying the interfaces for extractors
  * followUp: classes implementing filtering and mapping postprocessing steps
  * fromGeonames: extractors working on Geonames
  * fromOtherSources: extractors working on Wordnet, Wikidata, etc.
  * fromThemes: extractors depending on other extractors
  * fromWikipedia: extractors working on Wikipedia dumps
  * main: Contains the scheduler that starts the extractors

# Data Format 

In YAGO (as in RDF), each fact consists of a subject, a predicate, and an object. Every fact can have a fact id. This allows facts to talk about other facts. The fact id is simply computed as a hash from the subject, predicate, and object of the fact. An example fact is
```
<id_abcd> <Elvis_Presley>  <marriedTo>  <Priscilla_Presley>
```

### Entity names

Entity names follow the [RDF/Turtle convention](https://www.w3.org/TR/turtle/#sec-grammar-grammar). Turtle leaves some design choices open. We use the following conventions:
  1. YAGO entities are always given as relative URIs `<Albert_Einstein>`. This is because qnames may not contain certain characters
  2. All entities from standard namespaces are given as qnames. This is to save space and keep readability.
  3. All other entities are given as full URIs `<http://...>`
  4. The encoding is UTF-8, backslash encodings are used only if necessary, and `\uXXXX` encodings are avoided wherever possible.

We use predefined RDF entities wherever possible, in particular 

```
rdfs:domain, skos:prefLabel, rdfs:range, rdfs:label, rdfs:subClassOf,  
rdfs:subPropertyOf, rdf:type, rdf:Resource, xsd:boolean, rdf:Class, 
xsd:date, xsd:duration, rdf:Statement, xsd:integer, xsd:nonNegativeInteger, 
xsd:decimal, xsd:decimal, rdf:Property, xsd:string, xsd:gYear, owl:Thing
```

We integrate XML types into the YAGO literal type hierarchy. We use YAGO literal types as literal types in RDF. The root of the taxonomy of individuals (formerly "entity") is `owl:Thing`.

### Implementation 

Currently, all facts are in this format even inside the running program (i.e., inside Fact, FactCollection, etc.).
The implementation has to convert specifically to a Java String in order to work with real (16-bit) Java strings. This is done by FactComponent.asJavaString()

All frequent YAGO and RDFS string constants are declared in `basics.YAGO` and `basics.RDFS`, respectively.

### File format

YAGO can store facts either in TSV or in RDF/Turtle. All modules can deal with both formats, but we typically use TSV, because it is faster.

The TSV (Tab-Separated Values) format of YAGO contains 5 columns:
  * fact Id
  * Subject
  * Predicate
  * Object
  * Number: an optional column that contains the numeric value of the object

The RDF/Turtle format follows the standard Turtle conventions. To say that a fact `ABC` has a fact id `ID`, we use a comment in the line before the fact

```
     #@ ID
     ABC
```

# Licensing

### License

The source code of YAGO is licensed under GNU General Public License, version 3 or later.

The files generated by YAGO are licensed under Creative-Commons Attribution License.

### Licenses of used libraries

* Javatools: [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0)
* Basics3: [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0)
* JUnit: [Eclipse Public License 1.0](http://junit.org/junit4/license.html)

### Developers

The YAGO development is lead by (in alphabetical order):
* [Daniel Bär](https://www.linkedin.com/in/daniel-baer/)
* [Johannes Hoffart](http://www.mpi-inf.mpg.de/~jhoffart)
* [Thomas Rebele](https://thomasrebele.org) 
* [Fabian Suchanek](https://suchanek.name)

Contributors include (in alphabetical order):
* Joanna Biega
* Erdal Kuzey
* Farzaneh Mahdisoltani
* Ghazaleh Haratinezhad Torbati

### Citing YAGO

If you use the data of YAGO in your research, please cite:

```
    @inproceedings{yago,
      author    = {Fabian M. Suchanek and Gjergji Kasneci and Gerhard Weikum},
      title     = {{Yago: A Core of Semantic Knowledge}},
      booktitle = {16th International Conference on the World Wide Web},
      pages     = {697--706},
      year      = {2007}
    }
```

If you use the code YAGO in your research, please cite:

```
    @inproceedings{YAGO2016,
      author    = {Thomas Rebele and
                   Fabian M. Suchanek and
                   Johannes Hoffart and
                   Joanna Biega and
                   Erdal Kuzey and
                   Gerhard Weikum},
      title     = {{YAGO:} {A} Multilingual Knowledge Base from Wikipedia, Wordnet, and
                   Geonames},
      booktitle = {The Semantic Web - {ISWC} 2016 - 15th International Semantic Web Conference,
                   Kobe, Japan, October 17-21, 2016, Proceedings, Part {II}},
      pages     = {177--185},
      year      = {2016},
      url       = {https://doi.org/10.1007/978-3-319-46547-0_19},
      doi       = {10.1007/978-3-319-46547-0_19},
    }
```

### References

  * Fabian M. Suchanek, Gjergji Kasneci, and Gerhard Weikum. 2007. “Yago: A Core of Semantic Knowledge.” In Proceedings of the 16th International Conference on World Wide Web, 697–706. ACM. 
  * Suchanek, Fabian M., Gjergji Kasneci, and Gerhard Weikum. 2008. “Yago: A Large Ontology from Wikipedia and Wordnet.” Web Semantics: Science, Services and Agents on the World Wide Web 6 (3): 203–217.
  * Kasneci, Gjergji, Maya Ramanath, Fabian Suchanek, and Gerhard Weikum. 2009. “The YAGO-NAGA Approach to Knowledge Discovery.” ACM SIGMOD Record 37 (4): 41–47.
  * Hoffart, Johannes, Fabian M. Suchanek, Klaus Berberich, Edwin Lewis-Kelham, Gerard De Melo, and Gerhard Weikum. 2011. “YAGO2: Exploring and Querying World Knowledge in Time, Space, Context, and Many Languages.” In Proceedings of the 20th International Conference Companion on World Wide Web, 229–232. ACM. 
  * Biega, Joanna, Erdal Kuzey, and Fabian M. Suchanek. 2013. “Inside YAGO2s: A Transparent Information Extraction Architecture.” In Proceedings of the 22nd International Conference on World Wide Web Companion, 325–328. International World Wide Web Conferences Steering Committee. 
  * Hoffart, Johannes, Fabian M Suchanek, Klaus Berberich, and Gerhard Weikum. 2013. “YAGO2: A Spatially and Temporally Enhanced Knowledge Base from Wikipedia.” Artificial Intelligence 194: 28–61.
  * Suchanek, Fabian M., Johannes Hoffart, Erdal Kuzey, and Edwin Lewis-Kelham. 2013. “YAGO2s: Modular High-Quality Information Extraction with an Application to Flight Planning.” In BTW, 214:515–518. 
  * Mahdisoltani, Farzaneh, Joanna Biega, and Fabian Suchanek. 2015. “YAGO3: A Knowledge Base from Multilingual Wikipedias.” In 7th Biennial Conference on Innovative Data Systems Research. CIDR 2015. 
  * Rebele, Thomas, Fabian M. Suchanek, Johannes Hoffart, Joanna Biega, Erdal Kuzey, and Gerhard Weikum. 2016. “YAGO: A Multilingual Knowledge Base from Wikipedia, Wordnet, and Geonames.” In ISWC 2016.
