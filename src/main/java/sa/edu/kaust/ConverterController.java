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

		endpoint = "http://pro.knowitive.com/fuseki/Sample/query";

		query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "SELECT ?subject ?predicate ?object\n" + "WHERE {\n" + "?subject ?predicate ?object\n" + "}\n"
				+ "LIMIT 25";
		pattern = "?subject ?predicate ?object";

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
