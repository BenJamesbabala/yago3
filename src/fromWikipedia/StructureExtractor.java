package fromWikipedia;

import java.io.BufferedReader;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javatools.administrative.Announce;
import javatools.datatypes.FinalSet;
import javatools.filehandlers.FileLines;
import javatools.util.FileUtils;
import utils.FactCollection;
import utils.FactTemplateExtractor;
import utils.Theme;
import utils.TitleExtractor;
import basics.Fact;
import extractors.EnglishWikipediaExtractor;
import followUp.FollowUpExtractor;
import followUp.Redirector;
import followUp.TypeChecker;
import fromOtherSources.PatternHardExtractor;
import fromOtherSources.WordnetExtractor;

/**
 * Extracts Wikipedia links
 * 
 * @author Johannes Hoffart
 * 
 */
public class StructureExtractor extends EnglishWikipediaExtractor {

	@Override
	public Set<Theme> input() {
		return new HashSet<Theme>(Arrays.asList(
				PatternHardExtractor.STRUCTUREPATTERNS,
				PatternHardExtractor.TITLEPATTERNS,
				WordnetExtractor.PREFMEANINGS));
	}

	@Override
	public Set<FollowUpExtractor> followUp() {
		return new FinalSet<FollowUpExtractor>(new Redirector(
				DIRTYSTRUCTUREFACTS, REDIRECTEDSTRUCTUREFACTS, this),
				new TypeChecker(REDIRECTEDSTRUCTUREFACTS, STRUCTUREFACTS, this));
	}

	/** Facts representing the Wikipedia structure (e.g. links) */
	public static final Theme DIRTYSTRUCTUREFACTS = new Theme(
			"structureFactsNeedTypeCheckingRedirecting",
			"Regular structure from Wikipedia, e.g. links - needs redirecting and typechecking");

	/** Facts representing the Wikipedia structure (e.g. links) */
	public static final Theme REDIRECTEDSTRUCTUREFACTS = new Theme(
			"structureFactsNeedTypeChecking",
			"Regular structure from Wikipedia, e.g. links - needs typechecking");

	/** Facts representing the Wikipedia structure (e.g. links) */
	public static final Theme STRUCTUREFACTS = new Theme("structureFacts",
			"Regular structure from Wikipedia, e.g. links");

	@Override
	public Set<Theme> output() {
		return new FinalSet<Theme>(DIRTYSTRUCTUREFACTS);
	}

	@Override
	public void extract() throws Exception {
		// Extract the information
		Announce.doing("Extracting structure facts");

		BufferedReader in = FileUtils.getBufferedUTF8Reader(inputData);
		TitleExtractor titleExtractor = new TitleExtractor("en");

		FactCollection structurePatternCollection = PatternHardExtractor.STRUCTUREPATTERNS
				.factCollection();
		FactTemplateExtractor structurePatterns = new FactTemplateExtractor(
				structurePatternCollection, "<_extendedStructureWikiPattern>");

		String titleEntity = null;
		while (true) {
			switch (FileLines.findIgnoreCase(in, "<title>")) {
			case -1:
				Announce.done();
				in.close();
				return;
			case 0:
				titleEntity = titleExtractor.getTitleEntity(in);
				if (titleEntity == null)
					continue;

				String page = FileLines.readBetween(in, "<text", "</text>");
				String normalizedPage = page.replaceAll("[\\s\\x00-\\x1F]+",
						" ");

				for (Fact fact : structurePatterns.extract(normalizedPage,
						titleEntity)) {
					if (fact != null)
						DIRTYSTRUCTUREFACTS.write(fact);
				}
			}
		}
	}

	/**
	 * Needs Wikipedia as input
	 * 
	 * @param wikipedia
	 *            Wikipedia XML dump
	 */
	public StructureExtractor(File wikipedia) {
		super(wikipedia);
	}

}
