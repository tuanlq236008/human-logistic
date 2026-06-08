"""
Training Utilities for Model Fine-tuning
"""

from typing import List, Tuple
import numpy as np

# Import from local modules
from Core.models import Post, DamageType

# Check for sklearn availability
try:
    from sklearn.preprocessing import MultiLabelBinarizer
    SKLEARN_AVAILABLE = True
except ImportError:
    SKLEARN_AVAILABLE = False


class ModelTrainer:
    """Utility class để fine-tune models"""

    @staticmethod
    def prepare_sentiment_data(posts: List[Post], labels: List[str]) -> Tuple[List[str], List[int]]:
        """
        Chuẩn bị dữ liệu để train sentiment model
        Args:
            posts: List các posts
            labels: List nhãn tương ứng ['positive', 'negative', 'neutral']
        """
        texts = [post.content for post in posts]
        label_map = {'positive': 2, 'neutral': 1, 'negative': 0}
        encoded_labels = [label_map[label] for label in labels]

        return texts, encoded_labels

    @staticmethod
    def prepare_damage_data(posts: List[Post],
                           damage_labels: List[List[str]]) -> Tuple[List[str], np.ndarray]:
        """
        Chuẩn bị dữ liệu cho multi-label damage classification
        Args:
            posts: List các posts
            damage_labels: List of lists các loại thiệt hại cho từng post
        """
        if not SKLEARN_AVAILABLE:
            raise ImportError("Cần sklearn: pip install scikit-learn")

        texts = [post.content for post in posts]

        mlb = MultiLabelBinarizer(classes=DamageType.all_types())
        encoded_labels = mlb.fit_transform(damage_labels)

        return texts, encoded_labels, mlb