import pandas as pd
import re
import joblib
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, accuracy_score
from sklearn.feature_extraction.text import TfidfVectorizer

# === Smart text preprocessing ===
def smart_clean(text):
    text = text.lower()

    # Remove polite/extra words
    text = re.sub(r'\b(can you|could you|would you|please|kindly|for me|now|immediately)\b', '', text)

    # Remove punctuation and extra whitespace
    text = re.sub(r'[^\w\s]', '', text)
    text = re.sub(r'\s+', ' ', text).strip()

    return text

# === Load dataset ===
df = pd.read_csv("Datasets/dataset.csv")  # Make sure the CSV file is updated
df['text'] = df['text'].apply(smart_clean)

X = df['text']
y = df['command']

# === Train/test split ===
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# === Create pipeline: TF-IDF + Logistic Regression ===
pipeline = Pipeline([
    ('tfidf', TfidfVectorizer(ngram_range=(1, 2))),
    ('clf', LogisticRegression(max_iter=1000))
])

# === Train model ===
pipeline.fit(X_train, y_train)

# === Evaluate ===
y_pred = pipeline.predict(X_test)
print("Accuracy:", accuracy_score(y_test, y_pred))
print("\nReport:\n", classification_report(y_test, y_pred))

# === Save model pipeline as a single file ===
joblib.dump(pipeline, "nlp_intent_pipeline.pkl")
print("✅ Model pipeline saved as 'nlp_intent_pipeline_updated.pkl'")
