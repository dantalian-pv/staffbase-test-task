package de.pdmitriev.test.staffbase.storage

import de.pdmitriev.test.staffbase.storage.model.PersistAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class AnswersStorage(@Autowired val questionsStorage: QuestionsStorage) {
    protected val idGenerator = AtomicInteger()
    protected val questionAnswers = ConcurrentHashMap<Int, MutableList<PersistAnswer>>()
    protected val answers = ConcurrentHashMap<Int, PersistAnswer>()

    fun allAnswers(limit: Int = -1): List<PersistAnswer> {
        val allAnswersList = questionAnswers.flatMap { it.value }
        val sizeLimit = if (limit == -1) allAnswersList.size else limit
        return allAnswersList
                .sortedByDescending { it.creationDate }
                .subList(0, allAnswersList.size.coerceAtMost(sizeLimit))
    }

    fun answersToQuestion(questionId: Int, limit: Int = -1): List<PersistAnswer> {
        questionsStorage.getQuestion(questionId)
        val answerList = questionAnswers[questionId]?: mutableListOf()
        val sizeLimit = if (limit == -1) answerList.size else limit
        return answerList
                .sortedByDescending { it.creationDate }
                .subList(0, answerList.size.coerceAtMost(sizeLimit))
    }

    fun getAnswerById(id: Int): PersistAnswer {
        val answer = answers[id]
        return answer ?: throw NoEntityFoundException("No answer with $id found")
    }

    fun addAnswer(questionId: Int, content: String): PersistAnswer {
        questionsStorage.getQuestion(questionId)
        val answer = PersistAnswer(idGenerator.incrementAndGet(),
                questionId,
                content,
                System.currentTimeMillis())
        questionAnswers.computeIfAbsent(questionId) { mutableListOf() }.add(answer)
        answers[answer.id] = answer
        return answer
    }

    fun editAnswer(id: Int, content: String): PersistAnswer {
        val answer = getAnswerById(id)
        answer.content = content
        return answer
    }
}