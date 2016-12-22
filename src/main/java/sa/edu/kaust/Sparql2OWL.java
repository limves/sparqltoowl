package sa.edu.kaust;

import org.apache.jena.query.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.apache.jena.query.*;

import java.util.List;

public class Sparql2OWL {

	OWLOntology ontology;
	OWLDataFactory dataFactory;
	OWLOntologyManager manager;
	
	final String OWL_CLASS_TYPE = "rdf:type";

	public void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {

		final OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
		final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		final String iri = "www.somewhere.net/#";
		final OWLOntology ontology = manager.createOntology();

	}

	public ResultSet getSparqlResults(String sparqlQuery, String sparqlEndpoint) {

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution queryExec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
		ResultSet results = queryExec.execSelect();
		ResultSet results2 = queryExec.execSelect();
		ResultSetFormatter.out(results2);

		return results;

	}

	public OWLOntology createOntologyFromSparql(ResultSet results, String input, OWLOntology ontology,
			OWLDataFactory dataFactory, OWLOntologyManager manager, String iri)
			throws OWLOntologyStorageException, OWLOntologyCreationException, Exception {

		this.ontology = ontology;
		this.dataFactory = dataFactory;
		this.manager = manager;

		List<String> classList = results.getResultVars();

		String orig_input = new String(input);
		String input1 = input.replaceAll("\\(", " ").replaceAll("\\)", " ");
		System.out.println("input1:" + input1);
		String[] strArr = input1.split(" ");

		String domain = null;
		String range = null;
		String objectProperty = null;

		while (results.hasNext()) {

			Provider shortFormProvider = new Provider();
			OWLEntityChecker entityChecker = new ShortFormEntityChecker(shortFormProvider);
			QuerySolution querySolution = results.next();

			// input = input.replaceAll("\\?", "");
			int ind1 = classList.indexOf(strArr[0].replace("?", ""));
			int ind2 = classList.indexOf(strArr[1].replace("?", ""));
			int ind3 = classList.indexOf(strArr[2].replace("?", ""));
			String subject = querySolution.get(classList.get(ind1)).toString();
			String predicade = querySolution.get(classList.get(ind2)).toString();
			String object = querySolution.get(classList.get(ind3)).toString();

			// System.out.println(dataFactory.getOWLClass(IRI.create(predicade)).toString());

			if (OWL_CLASS_TYPE.equals(dataFactory.getOWLClass(IRI.create(predicade)).toString())) {
				if ("owl:Class".equals(dataFactory.getOWLClass(IRI.create(object)).toString())) {
					addClass(subject);
				}
			}

			// ObjectProperty

			if ("rdfs:domain".equals(dataFactory.getOWLClass(IRI.create(predicade)).toString())) {
				domain = dataFactory.getOWLClass(IRI.create(object)).toString();
			}
			if ("rdfs:range".equals(dataFactory.getOWLClass(IRI.create(predicade)).toString())) {
				range = dataFactory.getOWLClass(IRI.create(object)).toString();
			}
			if ("rdf:type".equals(dataFactory.getOWLClass(IRI.create(predicade)).toString())) {
				if ("owl:ObjectProperty".equals(dataFactory.getOWLClass(IRI.create(object)).toString())) {
					objectProperty = dataFactory.getOWLClass(IRI.create(subject)).toString();
				}
			}

			// TODO check if all the are the components are the same objectProperty
			if (objectProperty != null && domain != null && range != null) {
				domain = domain.replace("<", "").replace(">", "");
				range = range.replace("<", "").replace(">", "");
				objectProperty = objectProperty.replace("<", "").replace(">", "");

				addObjectProperty(addClass(domain), addClass(range), objectProperty);
				domain = objectProperty = range = null;
			}

			input = new String(orig_input);
		}

		return ontology;
	}

	public OWLClass addClass(String name) throws OWLOntologyStorageException, OWLOntologyCreationException, Exception {
		IRI iri = IRI.create(name);
		manager.setOntologyDocumentIRI(ontology, iri);
		OWLClass owlClass = dataFactory.getOWLClass(IRI.create(name));
		OWLAxiom axiom = dataFactory.getOWLDeclarationAxiom(owlClass);
		manager.addAxiom(ontology, axiom);
		return owlClass;
	}

	public OWLObjectProperty addObjectProperty(OWLClass domain, OWLClass range, String name) {

		OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(IRI.create(name));
		OWLObjectPropertyDomainAxiom domainAxiom = dataFactory.getOWLObjectPropertyDomainAxiom(objectProperty, domain);
		OWLObjectPropertyRangeAxiom rangeAxiom = dataFactory.getOWLObjectPropertyRangeAxiom(objectProperty, range);
		manager.addAxiom(ontology, domainAxiom);
		manager.addAxiom(ontology, rangeAxiom);
		return objectProperty;
	}

	public static String capitalize(String str) {
		return str;
	}

}
