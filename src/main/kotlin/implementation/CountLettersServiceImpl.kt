package implementation

import service.proto.CountLettersServiceGrpcKt.CountLettersServiceCoroutineImplBase
import service.proto.InputString
import service.proto.LetterNumber

class CountLettersServiceImpl : CountLettersServiceCoroutineImplBase() {
    override suspend fun countLetters(request: InputString): LetterNumber {
        val inputText = request.inputString
        var letterCount = inputText.length
        return LetterNumber.newBuilder().setLetterNumber(letterCount).build()
    }
}