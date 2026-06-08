"""
Damage Classification Classes
"""

from abc import ABC, abstractmethod
from typing import List, Dict, Tuple
import torch
import numpy as np
from transformers import AutoTokenizer, AutoModelForSequenceClassification

# Import from local modules
from Core.models import DamageType

# Check for transformers availability
try:
    TRANSFORMERS_AVAILABLE = True
except ImportError:
    TRANSFORMERS_AVAILABLE = False


class DamageClassifier(ABC):
    """Interface cho phân loại thiệt hại (Multi-label)"""

    @abstractmethod
    def classify(self, text: str) -> Tuple[List[str], Dict[str, float]]:
        """
        Returns:
            - List[str]: Danh sách loại thiệt hại
            - Dict[str, float]: Confidence score cho từng loại
        """
        pass

    @abstractmethod
    def batch_classify(self, texts: List[str]) -> List[Tuple[List[str], Dict[str, float]]]:
        pass


class BERTMultiLabelDamageClassifier(DamageClassifier):
    """
    Multi-label classification cho các loại thiệt hại
    Sử dụng BERT/PhoBERT với sigmoid output
    """

    def __init__(self, model_path: str = None, threshold: float = 0.5):
        """
        Args:
            model_path: Đường dẫn model đã fine-tune
            threshold: Ngưỡng để quyết định label (0-1)
        """
        if not TRANSFORMERS_AVAILABLE:
            raise ImportError("Cần cài đặt transformers")

        self.threshold = threshold
        self.label_names = DamageType.all_types()

        if model_path:
            print(f"🔧 Loading damage classifier từ {model_path}")
            self.model = AutoModelForSequenceClassification.from_pretrained(model_path)
            self.tokenizer = AutoTokenizer.from_pretrained(model_path)
            self.device = 'cuda' if torch.cuda.is_available() else 'cpu'
            self.model.to(self.device)
            self.model.eval()
        else:
            raise ValueError("Cần cung cấp model_path cho DamageClassifier")

    def classify(self, text: str) -> Tuple[List[str], Dict[str, float]]:
        results = self.batch_classify([text])
        return results[0]

    def batch_classify(self, texts: List[str], batch_size: int = 16) -> List[Tuple[List[str], Dict[str, float]]]:
        results = []

        for i in range(0, len(texts), batch_size):
            batch_texts = texts[i:i+batch_size]

            inputs = self.tokenizer(
                batch_texts,
                padding=True,
                truncation=True,
                max_length=256,
                return_tensors="pt"
            ).to(self.device)

            with torch.no_grad():
                outputs = self.model(**inputs)
                # Sigmoid cho multi-label
                probs = torch.sigmoid(outputs.logits)

            for prob in probs.cpu().numpy():
                # Lấy các labels có prob > threshold
                detected_labels = []
                scores = {}

                for idx, score in enumerate(prob):
                    label_name = self.label_names[idx]
                    scores[label_name] = float(score)

                    if score > self.threshold:
                        detected_labels.append(label_name)

                # Nếu không có label nào, chọn label có score cao nhất
                if not detected_labels:
                    max_label = self.label_names[np.argmax(prob)]
                    detected_labels.append(max_label)

                results.append((detected_labels, scores))

        return results


class HybridDamageClassifier(DamageClassifier):
    """
    Kết hợp Rule-based và ML
    - Rule-based: Nhanh, không cần train
    - ML: Chính xác cao, cần train
    """

    def __init__(self, ml_model_path: str = None, use_ml: bool = True):
        self.use_ml = use_ml and ml_model_path is not None

        # Rule-based keywords
        self.damage_keywords = {
            DamageType.AFFECTED_PEOPLE: {
                'người', 'dân', 'chết', 'mất tích', 'thương vong', 'nạn nhân',
                'bị thương', 'tử vong', 'gia đình', 'cộng đồng', 'bà con'
            },
            DamageType.ECONOMIC_DISRUPTION: {
                'kinh tế', 'sản xuất', 'kinh doanh', 'công ty', 'doanh nghiệp',
                'phá sản', 'đóng cửa', 'thiệt hại kinh tế', 'mất việc', 'thu nhập'
            },
            DamageType.HOUSING_DAMAGE: {
                'nhà', 'nhà cửa', 'mái', 'tường', 'sập', 'đổ', 'ngập nhà',
                'nhà bị', 'tòa nhà', 'công trình', 'chung cư', 'căn hộ'
            },
            DamageType.PROPERTY_LOSS: {
                'tài sản', 'đồ đạc', 'xe', 'máy móc', 'thiết bị', 'của cải',
                'mất trắng', 'cuốn trôi', 'mất sạch', 'đồ dùng', 'vật dụng'
            },
            DamageType.INFRASTRUCTURE_DAMAGE: {
                'đường', 'cầu', 'đê', 'điện', 'nước', 'giao thông', 'hạ tầng',
                'đường sá', 'cơ sở hạ tầng', 'công trình công cộng', 'trường'
            }
        }

        if self.use_ml:
            self.ml_classifier = BERTMultiLabelDamageClassifier(ml_model_path)

    def _rule_based_classify(self, text: str) -> Tuple[List[str], Dict[str, float]]:
        """Phân loại dựa trên keywords"""
        text_lower = text.lower()
        detected = []
        scores = {}

        for damage_type, keywords in self.damage_keywords.items():
            matches = sum(1 for kw in keywords if kw in text_lower)
            score = min(matches / 3.0, 1.0)  # Normalize
            scores[damage_type] = score

            if matches > 0:
                detected.append(damage_type)

        if not detected:
            detected.append(DamageType.OTHER)
            scores[DamageType.OTHER] = 1.0

        return detected, scores

    def classify(self, text: str) -> Tuple[List[str], Dict[str, float]]:
        if self.use_ml:
            return self.ml_classifier.classify(text)
        else:
            return self._rule_based_classify(text)

    def batch_classify(self, texts: List[str]) -> List[Tuple[List[str], Dict[str, float]]]:
        if self.use_ml:
            return self.ml_classifier.batch_classify(texts)
        else:
            return [self._rule_based_classify(text) for text in texts]