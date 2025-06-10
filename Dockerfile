# נשתמש בתמונה רזה של פייתון
FROM python:3.10-slim

# ניצור תיקייה לקוד
WORKDIR /app

# נעתיק את הקובץ app.py לקונטיינר
COPY app.py .

# נתקין את הספריות הדרושות
RUN pip install flask sentence-transformers

# נחשוף את הפורט 5000
EXPOSE 5000

# נריץ את הקובץ
CMD ["python", "app.py"]
