package org.jbehave.eclipse.editor.story;

import java.util.List;
import java.util.Locale;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.parser.StoryParser;
import org.jbehave.eclipse.parser.StoryPart;
import org.jbehave.eclipse.parser.StoryPartVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoryDocument extends Document {

	private Logger logger = LoggerFactory.getLogger(StoryDocument.class);

	private volatile List<StoryPart> parts;
	private JBehaveProject jbehaveProject;
	private Locale lastLocale;

	public StoryDocument() {
		super();
	}

	public void setJBehaveProject(JBehaveProject project) {
		this.jbehaveProject = project;
	}

	public JBehaveProject getJBehaveProject() {
		return jbehaveProject;
	}

	protected void fireDocumentChanged(DocumentEvent event) {
		invalidateStoryParts();
		// ... continue processing
		super.fireDocumentChanged(event);
	}

	private synchronized void invalidateStoryParts() {
		parts = null;
	}

	private synchronized List<StoryPart> getOrGenerateStoryParts() {
		logger.debug(
				"Retrieving story parts from document (previous locale {} current locale {})",
				lastLocale, jbehaveProject.getLocale());

		if (lastLocale == null
				|| !lastLocale.equals(jbehaveProject.getLocale())) {
			invalidateStoryParts();
			lastLocale = jbehaveProject.getLocale();
		}
		if (parts == null) {
			parts = new StoryParser(jbehaveProject.getLocalizedStepSupport())
					.parse(get());
		}
		return parts;
	}

	public void traverseStoryParts(StoryPartVisitor visitor) {
		for (StoryPart part : getOrGenerateStoryParts()) {
			visitor.visit(part);
			if (visitor.isDone())
				return;
		}
	}
}
