package com.google.sps.model;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import java.io.IOException;

public class SentimentEvaluator {

	public static float getSentiment(String message) throws IOException {
		Document doc =
        	Document.newBuilder().setContent(message).setType(Document.Type.PLAIN_TEXT).build();

        LanguageServiceClient languageService = LanguageServiceClient.create();
        Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
        float score = sentiment.getScore();
        languageService.close();
        
		return score;
	}

}