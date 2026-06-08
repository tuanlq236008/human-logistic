"""
Processing Classes for Batch Analysis
"""

from typing import List

# Import from local modules
from Core.models import Post
from Analyzer.sentiment_analyzer import SentimentAnalyzer
from Analyzer.damage_classifier import DamageClassifier


class SentimentAnalysisProcessor:
    """Xử lý phân tích tâm lý cho toàn bộ dataset"""

    def __init__(self, analyzer: SentimentAnalyzer):
        self.analyzer = analyzer

    def process(self, posts: List[Post], batch_size: int = 32) -> List[Post]:
        """Phân tích tâm lý với batch processing"""
        print(f"🔄 Đang phân tích tâm lý cho {len(posts)} posts...")

        texts = [post.content for post in posts]
        results = self.analyzer.batch_analyze(texts, batch_size=batch_size)

        for post, sentiment in zip(posts, results):
            post.sentiment = sentiment.label
            post.sentiment_scores = sentiment

        print("✅ Hoàn thành phân tích tâm lý")
        return posts


class DamageClassificationProcessor:
    """Xử lý phân loại thiệt hại cho toàn bộ dataset"""

    def __init__(self, classifier: DamageClassifier):
        self.classifier = classifier

    def process(self, posts: List[Post], batch_size: int = 32) -> List[Post]:
        """Phân loại thiệt hại với batch processing"""
        print(f"🔄 Đang phân loại thiệt hại cho {len(posts)} posts...")

        texts = [post.content for post in posts]
        results = self.classifier.batch_classify(texts)

        for post, (labels, scores) in zip(posts, results):
            post.damage_types = labels
            post.damage_scores = scores

        print("✅ Hoàn thành phân loại thiệt hại")
        return posts