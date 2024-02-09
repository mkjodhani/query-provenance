# Query Provenance

This GitHub repository hosts a project that focuses on enhancing data management capabilities by providing "explanations" for the tuples obtained in a query result. In contemporary data management, the need for transparent and informative query results is ever-increasing. This project addresses this need by extending the traditional relational data model to support annotated relations.

## Table of Contents
- [Introduction](#introduction)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Contributing](#contributing)
- [Contact](#contact)
- [License](#license)

## Introduction

This project aims to enhance data management by providing detailed "explanations" for tuples in query results. It extends the relational data model to annotated relations. Annotated relations have extra attributes that record additional information for each tuple. The primary focus is on providing annotations that help explain how each tuple in a query result was obtained.

This project is based on the concepts described in the paper by [Green et al (PODS 2007)](assets/paper/pods07.pdf), which is provided in the project folder. It introduces the concept of "explanations," which are synonymous with provenance in the literature.

## Annotated Relations

In our extended model, if we have a standard relation or table, denoted as R(A1, ..., An), we create an annotated relation, R'(A1, ..., An, I), where I is an additional attribute or column. This new attribute, I, records supplementary information for each tuple in R'. The key innovation of this project is that the query processor module operates on these tuple annotations to produce query results that are also annotated relations.

## Tuple IDs and Lineage

An essential aspect of our approach is the use of tuple IDs. These IDs serve to uniquely identify each tuple within a relation or across the entire database. Tuple IDs play a crucial role in explaining how each tuple in a query result was obtained using the tuples in the database. This explanation, or annotation, is generated during the query processing. In its simplest form, the explanation associates with each tuple, t, the set of IDs of contributing tuples, referred to as "lineage." However, annotations can encompass various attributes such as multiplicity or probability/certainty of tuples, depending on the specific use case.

## Provenance and Explanations

In the academic literature, the term "provenance" is often used interchangeably with what we call "explanations" in this project. We define provenance or explanations as additional information associated with a tuple to clarify its origins within the database.

## Project Goals

The primary objective of this project is to implement a query processor that evaluates SQL queries involving annotated relations and produces annotated query results. To illustrate this, we will consider a database, D, containing annotated relations R(A1, A2, ..., An, I) and S(B1, B2, ..., Bm, J). The query, Q, will join R and S, and the results will be stored as an annotated relation, T(A1, ..., An, B1, ..., Bm, K), where K represents the annotations of the tuples in T. Each tuple in T is associated with an annotation, which is a collection of annotations from tuples in R and S that contributed to it.

## Getting Started
- Download the library from <a href="https://raw.githubusercontent.com/mkjodhani/query-provenance/master/assets/artifacts/query-provenance.jar" download="query-provenance.jar">here</a>.
- Add JAR file to your project dependency.
    - Using gradle plugin, add following line to `gradle.properties`
        ```
        dependencies {
            // ...other depedencies 
            implementation files('lib/query-provenance.jar')
        }
        ```
    - if not, follow standard procedure to add JAR files to your project.


## Resources
- [Documentation](https://mkjodhani.com/docs/query-provenance/)
- [Changelog](/CHANGELOG.md)

## Usage

To display the associated annotation for any tuple in T, you can use the provided interface. The interface allows you to query the annotations for individual tuples in the result. Depending on the annotation attributes, you can retrieve lineage information or other relevant explanations for each tuple.

Please refer to the project folder for the paper by [Green et al (PODS 2007)](assets/paper/pods07.pdf), which provides further details, example relation instances, and queries to better understand the types and applications of explanations in this project.

## Contributing

If you have questions or need assistance with the project or your contributions, please feel free to contact me. You can reach out to me through GitHub by creating an issue or using the contact information provided in the repository.

I look forward to collaborating with you and appreciate your interest in contributing to this project!

## Contact

If you encounter any bugs, have questions, or want to discuss the project further, please feel free to reach out to us.

- [Email](mailto:contact@mkjodhani.com)
- [GitHub Issues](https://github.com/mkjodhani/query-provenance/issues)

## License

This project is licensed under the [MIT License](LICENSE) - see the [LICENSE](/LICENSE) file for details.
