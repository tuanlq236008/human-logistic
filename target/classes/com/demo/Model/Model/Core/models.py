"""
Data Models for Disaster Analysis System
"""

from datetime import datetime
from typing import Dict, List


class Post:
    """Đại diện cho một bài đăng từ mạng xã hội"""

    def __init__(self, post_id: str, authors: str, keyword: str,
                 date: str, reactions: int, comments: int,
                 shares: int, content: str):
        self.post_id = post_id
        self.authors = authors
        self.keyword = keyword
        self.date = self._parse_date(date)
        self.reactions = reactions
        self.comments = comments
        self.shares = shares
        self.content = content
        self.sentiment = None
        self.sentiment_scores = None  # Scores chi tiết (positive, negative, neutral)
        self.damage_types = []
        self.damage_scores = {}  # Confidence scores cho từng loại thiệt hại

    def _parse_date(self, date_str: str) -> datetime:
        """Chuyển đổi chuỗi ngày thành datetime"""
        try:
            return datetime.strptime(date_str, '%Y-%m-%d')
        except:
            try:
                return datetime.strptime(date_str, '%Y-%m-%d')
            except:
                return None

    def __repr__(self):
        return f"Post(id={self.post_id}, date={self.date}, keyword={self.keyword})"


class SentimentResult:
    """Kết quả phân tích tâm lý với confidence scores"""

    def __init__(self, positive: float, negative: float, neutral: float):
        self.positive = positive
        self.negative = negative
        self.neutral = neutral
        self.label = self._get_label()
        self.confidence = max(positive, negative, neutral)

    def _get_label(self) -> str:
        scores = {'positive': self.positive, 'negative': self.negative, 'neutral': self.neutral}
        return max(scores, key=scores.get)

    def to_dict(self) -> Dict:
        return {
            'label': self.label,
            'positive': float(self.positive),
            'negative': float(self.neutral),
            'neutral': float(self.neutral),
            'confidence': float(self.confidence)
        }

    def __repr__(self):
        return f"Sentiment({self.label}: conf={self.confidence:.3f})"


class DamageType:
    """Các loại thiệt hại - có thể mở rộng"""

    AFFECTED_PEOPLE = "Người bị ảnh hưởng"
    ECONOMIC_DISRUPTION = "Gián đoạn kinh tế"
    HOUSING_DAMAGE = "Nhà cửa bị hư hỏng"
    PROPERTY_LOSS = "Tài sản bị mất"
    INFRASTRUCTURE_DAMAGE = "Cơ sở hạ tầng bị hư hỏng"
    OTHER = "Khác"

    @classmethod
    def all_types(cls) -> List[str]:
        return [
            cls.AFFECTED_PEOPLE,
            cls.ECONOMIC_DISRUPTION,
            cls.HOUSING_DAMAGE,
            cls.PROPERTY_LOSS,
            cls.INFRASTRUCTURE_DAMAGE,
            cls.OTHER
        ]

    @classmethod
    def get_label_mapping(cls) -> Dict[str, int]:
        """Mapping cho multi-label classification"""
        return {label: idx for idx, label in enumerate(cls.all_types())}