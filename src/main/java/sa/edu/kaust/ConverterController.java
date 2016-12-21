package sa.edu.kaust;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.jena.query.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import sa.edu.kaust.Sparql2OWL;

public class ConverterController {

	private final static String iri = "www.somewhere.net/#";
	OWLDataFactory dataFactory;
	OWLOntologyManager manager;
	Sparql2OWL sparql2OWL;

	public ConverterController() {
		this.dataFactory = OWLManager.getOWLDataFactory();
		this.manager = OWLManager.createOWLOntologyManager();
		this.sparql2OWL = new Sparql2OWL();
	}

	public static void main(String[] args) {

		ConverterController cc = new ConverterController();
		cc.convert("http://rdf.disgenet.org/sparql/", "Select * {?s ?p ?o} LIMIT 2", "", "");

	}

	public void convert(String endpoint, String query, String pattern, String refOnts) {

		/*
		query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX dcterms: <http://purl.org/dc/terms/>\n"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
				+ "PREFIX sio: <http://semanticscience.org/resource/>\n"
				+ "PREFIX ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>\n"
				+ "SELECT DISTINCT ?disease ?phenotype WHERE {\n" + "?gda sio:SIO_000628 ?disease,?gene .\n"
				+ "?disease rdf:type ncit:C7057 .\n" + "?disease skos:exactMatch ?test .\n"
				+ "?test sio:SIO_001279 ?phenotype .\n"
				+ "?disease dcterms:title ?diseaseName . FILTER (str(?diseaseName) = 'Obesity')\n" + "} limit 20";
		pattern = "?disease SubClassOf(has-phenotype some ?phenotype)";
*/
	
		 endpoint = "http://pro.knowitive.com/fuseki/sample/query";
		 query =  "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "SELECT ?subject ?predicate ?object\n"
				+ "WHERE {\n"
				+ "?subject ?predicate ?object\n"
				+ "}\n"
				+ "LIMIT 25";
			pattern = "?object SubClassOf(?subject some ?predicate)";


		try {
			// OutputStream os = response.getOutputStream();
			OutputStream out = new ByteArrayOutputStream();
			OWLOntology ontology = manager.createOntology();

			// upon submmit
			String separator = ":::[\\r\\n]+";
			String[] endpoints = endpoint.split(separator);
			String[] queries = query.split(separator);
			String[] patterns = pattern.split(separator);

			for (int i = 0; i < queries.length; i++) {
				ResultSet results = this.sparql2OWL.getSparqlResults(queries[i], endpoints[i]);
				OWLOntology newOntology = this.sparql2OWL.createOntologyFromSparql(results, patterns[i], ontology,
						dataFactory, manager, iri);
			}
			String[] imports = refOnts.split(",");
			for (String import1 : imports) {
				OWLImportsDeclaration imp = manager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create(import1));
				manager.applyChange(new AddImport(ontology, imp));
			}

			manager.saveOntology(ontology, out);
			System.out.println(out.toString());

			// os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
