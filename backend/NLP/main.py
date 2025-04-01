import pandas as pd
import re
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
import joblib

# Clean text function
def clean_text(text):
    # Convert to lowercase and remove punctuation
    text = text.lower()
    text = re.sub(r'\s+', ' ', text)  # Replace multiple spaces with a single space
    text = re.sub(r'[^\w\s]', '', text)  # Remove punctuation
    return text

# Load dataset
df = pd.read_csv("dataset.csv")

# Clean the text data
df['text'] = df['text'].apply(clean_text)

# Split data
X = df['text']
y = df['command']

# Vectorize the text data (with n-grams)
vectorizer = TfidfVectorizer(ngram_range=(1, 2))
X_vec = vectorizer.fit_transform(X)

# Split into training and test sets
X_train, X_test, y_train, y_test = train_test_split(X_vec, y, test_size=0.2, random_state=42)

# Train the model
model = MultinomialNB()
model.fit(X_train, y_train)

# Test the model
y_pred = model.predict(X_test)
print(f"Accuracy: {accuracy_score(y_test, y_pred):.2f}")

# Save the model and vectorizer
joblib.dump(model, "nlp_command_model.pkl")
joblib.dump(vectorizer, "nlp_vectorizer.pkl")

print("Model and vectorizer saved successfully!")

# Testing code for new inputs
def predict_command(text):
    text = clean_text(text)  # Clean the input text
    text_vec = vectorizer.transform([text])
    prediction = model.predict(text_vec)
    return prediction[0]

# Test with new inputs
test_commands = [
    "make the room darker"
]

for command in test_commands:
    predicted_command = predict_command(command)
    print(f"Input: {command} -> Predicted Command: {predicted_command}")

