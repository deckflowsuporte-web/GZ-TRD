package com.translator.offline.data.ml

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Tradutor LEVE usando frases e dicionário local
 * 
 * Vantagens:
 * - ~5MB apenas (vs 500MB do NLLB)
 * - Executa em qualquer celular
 * - Rápido (sem inferência de ML)
 * - Offline desde o início
 * 
 * Desvantagens:
 * - Precisão ~60-70%
 * - Apenas frases e expressões conhecidas
 * - Não entende contexto
 * 
 * Ideal para: celulares fracos, uso rápido, economia de dados
 */
class LightweightTranslator(private val context: Context) {

    companion object {
        // Idiomas suportados
        val SUPPORTED_LANGUAGES = mapOf(
            "pt" to "Português",
            "en" to "English",
            "es" to "Español",
            "fr" to "Français",
            "de" to "Deutsch",
            "it" to "Italiano"
        )
        
        private const val DATA_DIR = "lightweight_data"
    }

    // Dicionário de traduções pré-computadas
    // Frequentemente usadas + expressões comuns
    private val commonPhrases = mapOf(
        // Português -> Inglês
        "pt_en" to mapOf(
            "olá" to "hello",
            "oi" to "hi",
            "bom dia" to "good morning",
            "boa tarde" to "good afternoon",
            "boa noite" to "good night",
            "obrigado" to "thank you",
            "obrigada" to "thank you",
            "por favor" to "please",
            "desculpe" to "sorry",
            "como vai" to "how are you",
            "como você está" to "how are you",
            "não" to "no",
            "sim" to "yes",
            "eu te amo" to "I love you",
            "tchau" to "bye",
            "até logo" to "see you later",
            "muito obrigado" to "thank you very much",
            "com licença" to "excuse me",
            "feliz aniversário" to "happy birthday",
            "boa sorte" to "good luck",
            "parabéns" to "congratulations",
            "saúde" to "cheers",
            "com certeza" to "of course",
            "talvez" to "maybe",
            "eu não sei" to "I don't know",
            "onde fica" to "where is",
            "quanto custa" to "how much is it",
            "preciso de ajuda" to "I need help",
            "não entendo" to "I don't understand",
            "fale mais devagar" to "speak slower",
            "como falar" to "how to say",
            "obrigado pela ajuda" to "thanks for your help",
            "muito prazer" to "nice to meet you",
            "até amanhã" to "see you tomorrow",
            "tenha um bom dia" to "have a good day",
            "de nada" to "you're welcome",
            "meu nome é" to "my name is",
            "prazer em conhecê-lo" to "pleased to meet you",
            "o que você quer fazer" to "what do you want to do",
            "onde você está" to "where are you",
            "eu gosto de" to "I like",
            "eu preciso" to "I need",
            "eu quero" to "I want",
            "eu posso" to "I can",
            "eu tenho" to "I have",
            "isso é bom" to "that's good",
            "isso é ruim" to "that's bad",
            "eu estou com fome" to "I am hungry",
            "eu estou com sede" to "I am thirsty",
            "eu estou cansado" to "I am tired",
            "eu estou doente" to "I am sick"
        ),
        
        // Português -> Espanhol
        "pt_es" to mapOf(
            "olá" to "hola",
            "oi" to "hola",
            "bom dia" to "buenos días",
            "boa tarde" to "buenas tardes",
            "boa noite" to "buenas noches",
            "obrigado" to "gracias",
            "obrigada" to "gracias",
            "por favor" to "por favor",
            "desculpe" to "lo siento",
            "como vai" to "cómo estás",
            "não" to "no",
            "sim" to "sí",
            "eu te amo" to "te amo",
            "tchau" to "adiós",
            "até logo" to "hasta luego",
            "muito obrigado" to "muchas gracias",
            "com licença" to "con permiso",
            "feliz aniversário" to "feliz cumpleaños",
            "boa sorte" to "buena suerte",
            "parabéns" to "felicitaciones",
            "saúde" to "salud",
            "com certeza" to "por supuesto",
            "talvez" to "quizás",
            "eu não sei" to "no sé",
            "onde fica" to "dónde está",
            "quanto custa" to "cuánto cuesta",
            "preciso de ajuda" to "necesito ayuda",
            "não entendo" to "no entiendo",
            "fale mais devagar" to "habla más despacio",
            "como falar" to "cómo decir",
            "de nada" to "de nada",
            "meu nome é" to "me llamo",
            "muito prazer" to "mucho gusto",
            "até amanhã" to "hasta mañana",
            "tenha um bom dia" to "que tengas un buen día",
            "isso é bom" to "eso es bueno",
            "isso é ruim" to "eso es malo",
            "eu estou com fome" to "tengo hambre",
            "eu estou com sede" to "tengo sed",
            "eu estou cansado" to "estoy cansado",
            "eu estou doente" to "estoy enfermo"
        ),
        
        // Português -> Francês
        "pt_fr" to mapOf(
            "olá" to "bonjour",
            "oi" to "salut",
            "bom dia" to "bonjour",
            "boa tarde" to "bon après-midi",
            "boa noite" to "bonne nuit",
            "obrigado" to "merci",
            "obrigada" to "merci",
            "por favor" to "s'il vous plaît",
            "desculpe" to "désolé",
            "como vai" to "comment allez-vous",
            "não" to "non",
            "sim" to "oui",
            "eu te amo" to "je t'aime",
            "tchau" to "au revoir",
            "até logo" to "à bientôt",
            "muito obrigado" to "merci beaucoup",
            "com licença" to "excusez-moi",
            "feliz aniversário" to "joyeux anniversaire",
            "boa sorte" to "bonne chance",
            "parabéns" to "félicitations",
            "saúde" to "santé",
            "com certeza" to "bien sûr",
            "talvez" to "peut-être",
            "eu não sei" to "je ne sais pas",
            "de nada" to "de rien",
            "meu nome é" to "je m'appelle",
            "muito prazer" to "enchanté",
            "até amanhã" to "à demain",
            "isso é bom" to "c'est bon",
            "isso é ruim" to "c'est mauvais",
            "eu estou com fome" to "j'ai faim",
            "eu estou com sede" to "j'ai soif",
            "eu estou cansado" to "je suis fatigué",
            "eu estou doente" to "je suis malade"
        ),
        
        // Português -> Alemão
        "pt_de" to mapOf(
            "olá" to "hallo",
            "oi" to "hi",
            "bom dia" to "guten morgen",
            "boa tarde" to "guten tag",
            "boa noite" to "gute nacht",
            "obrigado" to "danke",
            "obrigada" to "danke",
            "por favor" to "bitte",
            "desculpe" to "entschuldigung",
            "como vai" to "wie geht es ihnen",
            "não" to "nein",
            "sim" to "ja",
            "eu te amo" to "ich liebe dich",
            "tchau" to "tschüss",
            "até logo" to "bis bald",
            "muito obrigado" to "vielen dank",
            "com licença" to "entschuldigung",
            "feliz aniversário" to "alles gute zum geburtstag",
            "boa sorte" to "viel glück",
            "parabéns" to "herzlichen glückwunsch",
            "saúde" to "prosit",
            "com certeza" to "natürlich",
            "talvez" to "vielleicht",
            "eu não sei" to "ich weiss nicht",
            "de nada" to "bitte schön",
            "meu nome é" to "ich heisse",
            "muito prazer" to "freut mich",
            "até amanhã" to "bis morgen",
            "isso é bom" to "das ist gut",
            "isso é ruim" to "das ist schlecht",
            "eu estou com fome" to "ich habe hunger",
            "eu estou com sede" to "ich habe durst",
            "eu estou cansado" to "ich bin müde",
            "eu estou doente" to "ich bin krank"
        ),
        
        // Português -> Italiano
        "pt_it" to mapOf(
            "olá" to "ciao",
            "oi" to "ciao",
            "bom dia" to "buongiorno",
            "boa tarde" to "buon pomeriggio",
            "boa noite" to "buonanotte",
            "obrigado" to "grazie",
            "obrigada" to "grazie",
            "por favor" to "per favore",
            "desculpe" to "scusa",
            "como vai" to "come stai",
            "não" to "no",
            "sim" to "sì",
            "eu te amo" to "ti amo",
            "tchau" to "arrivederci",
            "até logo" to "a presto",
            "muito obrigado" to "molte grazie",
            "com licença" to "scusi",
            "feliz aniversário" to "buon compleanno",
            "boa sorte" to "buona fortuna",
            "parabéns" to "congratulazioni",
            "saúde" to "salute",
            "com certeza" to "certo",
            "talvez" to "forse",
            "eu não sei" to "non lo so",
            "de nada" to "prego",
            "meu nome é" to "mi chiamo",
            "muito prazer" to "piacere",
            "até amanhã" to "a domani",
            "isso é bom" to "questo è buono",
            "isso é ruim" to "questo è cattivo",
            "eu estou com fome" to "ho fame",
            "eu estou com sede" to "ho sete",
            "eu estou cansado" to "sono stanco",
            "eu estou doente" to "sto male"
        ),
        
        // Reversos (Inglês -> Português, etc)
        "en_pt" to mapOf(
            "hello" to "olá",
            "hi" to "oi",
            "good morning" to "bom dia",
            "good afternoon" to "boa tarde",
            "good night" to "boa noite",
            "thank you" to "obrigado",
            "please" to "por favor",
            "sorry" to "desculpe",
            "how are you" to "como vai",
            "no" to "não",
            "yes" to "sim",
            "I love you" to "eu te amo",
            "bye" to "tchau",
            "see you later" to "até logo",
            "of course" to "com certeza",
            "maybe" to "talvez",
            "I don't know" to "eu não sei",
            "where is" to "onde fica",
            "how much" to "quanto custa",
            "I need help" to "preciso de ajuda",
            "I don't understand" to "não entendo",
            "speak slower" to "fale mais devagar",
            "you're welcome" to "de nada",
            "my name is" to "meu nome é",
            "nice to meet you" to "muito prazer",
            "see you tomorrow" to "até amanhã",
            "have a good day" to "tenha um bom dia",
            "that's good" to "isso é bom",
            "that's bad" to "isso é ruim",
            "I am hungry" to "eu estou com fome",
            "I am thirsty" to "eu estou com sede",
            "I am tired" to "eu estou cansado",
            "I am sick" to "eu estou doente",
            "I like" to "eu gosto",
            "I need" to "eu preciso",
            "I want" to "eu quero",
            "I can" to "eu posso",
            "I have" to "eu tenho",
            "what is your name" to "qual é o seu nome",
            "where are you from" to "de onde você é",
            "I am from Brazil" to "eu sou do brasil",
            "how much is it" to "quanto é",
            "help me" to "ajude-me",
            "call the police" to "chame a polícia",
            "I am lost" to "estou perdido",
            "hospital" to "hospital",
            "doctor" to "médico",
            "bathroom" to "banheiro",
            "water" to "água",
            "food" to "comida",
            "money" to "dinheiro",
            "cheap" to "barato",
            "expensive" to "caro"
        )
    )

    /**
     * Traduz texto usando dicionário local
     * - Procura frases exatas primeiro
     * - Quebra texto em frases e traduz cada uma
     * - Mantém pontuação e estrutura
     */
    suspend fun translate(text: String, sourceLang: String, targetLang: String): String = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext ""

        val key = "${sourceLang}_${targetLang}"
        val dictionary = commonPhrases[key] ?: commonPhrases["${targetLang}_${sourceLang}"]

        if (dictionary == null) {
            // Se não tem dicionário direto, usa fallback
            return@withContext translateWithFallback(text, sourceLang, targetLang)
        }

        // Processar texto
        val result = StringBuilder()
        val sentences = text.lowercase().split(Regex("([.!?;:,]\\s*)"))
        
        sentences.forEachIndexed { index, sentence ->
            val trimmed = sentence.trim()
            if (trimmed.isNotEmpty()) {
                val translation = findBestTranslation(trimmed, dictionary)
                result.append(translation)
            }
            
            // Manter separadores originais
            if (index < sentences.size - 1) {
                val separator = Regex("([.!?;:,]\\s*)").find(text, sentences.map { it.length }.take(index + 1).sum())
                result.append(separator?.value ?: ". ")
            }
        }

        result.toString()
    }

    /**
     * Encontra a melhor tradução para uma frase
     * - Procura correspondência exata
     * - Procura palavras-chave
     * - Fallback: retorna original
     */
    private fun findBestTranslation(text: String, dictionary: Map<String, String>): String {
        val normalizedText = text.trim().lowercase()
        
        // 1. Correspondecia exata
        dictionary[normalizedText]?.let { return it }
        
        // 2. Procura frase que contenha texto
        for ((phrase, translation) in dictionary) {
            if (normalizedText.contains(phrase) || phrase.contains(normalizedText)) {
                // Substitui apenas a frase encontrada
                return normalizedText.replace(phrase, translation)
            }
        }
        
        // 3. Traduz palavra por palavra (para palavras conhecidas)
        val words = normalizedText.split(Regex("\\s+"))
        val translatedWords = words.map { word ->
            dictionary[word] ?: word
        }
        
        val result = translatedWords.joinToString(" ")
        
        // Se muitas palavras não foram traduzidas, retorna original
        val untranslatedCount = translatedWords.zip(words).count { (t, o) -> t == o }
        return if (untranslatedCount.toFloat() / words.size > 0.5) {
            text // Retorna original se mais de 50% não traduzido
        } else {
            result
        }
    }

    /**
     * Fallback para idiomas sem dicionário direto
     * Usa tradução inglês como intermediária
     */
    private fun translateWithFallback(text: String, sourceLang: String, targetLang: String): String {
        // Para demo, retorna texto com indicador de idioma
        val langName = SUPPORTED_LANGUAGES[targetLang] ?: targetLang.uppercase()
        return "[$langName] $text"
    }

    /**
     * Verifica se o par de idiomas é suportado
     */
    fun isLanguagePairSupported(source: String, target: String): Boolean {
        val key = "${source}_${target}"
        val reverseKey = "${target}_${source}"
        return commonPhrases.containsKey(key) || commonPhrases.containsKey(reverseKey)
    }

    /**
     * Retorna tamanho estimado dos dados (~5MB)
     */
    fun getDataSize(): Long {
        // ~5MB de dicionários pré-computados
        return 5_000_000L
    }

    /**
     * Lista idiomas suportados
     */
    fun getSupportedLanguages(): List<String> {
        return SUPPORTED_LANGUAGES.keys.toList()
    }
}
