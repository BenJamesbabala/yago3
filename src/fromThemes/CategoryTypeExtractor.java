package fromThemes;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import utils.MultilingualTheme;
import utils.Theme;
import javatools.administrative.Announce;
import javatools.datatypes.FinalSet;
import basics.Fact;
import basics.FactComponent;
import basics.FactSource;
import extractors.MultilingualExtractor;
import fromWikipedia.CategoryExtractor;

/**
 * WikipediaTypeExtractor - YAGO2s
 * 
 * Extracts types from category membership facts.
 * 
 * @author Fabian
 * 
 */
public class CategoryTypeExtractor extends MultilingualExtractor {

	/** Sources for category facts */
	public static final MultilingualTheme CATEGORYTYPESOURCES = new MultilingualTheme(
			"categoryTypeSources",
			"Sources for the classes derived from the Wikipedia categories, with their connection to the WordNet class hierarchy leaves");

	/** Types deduced from categories */
	public static final MultilingualTheme CATEGORYTYPES = new MultilingualTheme(
			"categoryTypes",
			"The rdf:type facts of YAGO derived from the categories");

	public Set<Theme> input() {
		Set<Theme> result = new TreeSet<Theme>(
				Arrays.asList(CategoryClassExtractor.CATEGORYCLASSES));
		if (isEnglish())
			result.add(CategoryExtractor.CATEGORYMEMBERS.inLanguage(language));
		else
			result.add(CategoryExtractor.CATEGORYMEMBERS_TRANSLATED
					.inLanguage(language));
		return result;
	}

	@Override
	public Set<Theme> inputCached() {
		return new FinalSet<>(CategoryClassExtractor.CATEGORYCLASSES);
	}

	@Override
	public Set<Theme> output() {
		return new FinalSet<Theme>(CATEGORYTYPESOURCES.inLanguage(language),
				CATEGORYTYPES.inLanguage(language));
	}

	@Override
	public void extract() throws Exception {
		Set<String> validClasses = CategoryClassExtractor.CATEGORYCLASSES
				.factCollection().getSubjects();

		FactSource categoryMembs;
		if (isEnglish())
			categoryMembs = CategoryExtractor.CATEGORYMEMBERS
					.inLanguage(language);
		else
			categoryMembs = CategoryExtractor.CATEGORYMEMBERS_TRANSLATED
					.inLanguage(language);

		// Extract the information
		for (Fact f : categoryMembs) {
			if (!f.getRelation().equals("<hasWikiCategory>"))
				continue;
			String category = f.getObject();
			if (!validClasses.contains(category))
				continue;
			write(CATEGORYTYPES.inLanguage(language), new Fact(f.getSubject(),
					"rdf:type", category),
					CATEGORYTYPESOURCES.inLanguage(language),
					FactComponent.wikipediaURL(f.getSubject()),
					"By membership in conceptual category");
		}
		Announce.done();
	}

	public CategoryTypeExtractor(String lang) {
		super(lang);
	}

	public static void main(String[] args) throws Exception {
		new CategoryTypeExtractor("en").extract(
				new File("c:/fabian/data/yago3"), "Test");
	}

}
