/* Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.languagetool

import org.bsplines.ltexls.parsing.AnnotatedTextFragment
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

abstract class LanguageToolInterface {
  var dictionary: Set<String> = emptySet()
  var dictionaryFile: String = ""
  var disabledRules: Set<String> = emptySet()

  var languageToolOrgUsername = ""
  var languageToolOrgApiKey = ""

  fun check(annotatedTextFragment: AnnotatedTextFragment): List<LanguageToolRuleMatch> {
    val matches = ArrayList<LanguageToolRuleMatch>()

    for (match: LanguageToolRuleMatch in checkInternal(annotatedTextFragment)) {
      if (checkMatchValidity(annotatedTextFragment, match)) matches.add(match)
    }

    return matches
  }

  protected fun checkMatchValidity(
    annotatedTextFragment: AnnotatedTextFragment,
    match: LanguageToolRuleMatch,
  ): Boolean {
    val dictionary = when (Files.isRegularFile(Paths.get(dictionaryFile))) {
      true -> this.dictionary.plus(File(dictionaryFile).readLines())
      false -> this.dictionary
    }
    return (
      (
        !match.isUnknownWordRule()
        || !dictionary.contains(
          annotatedTextFragment.getSubstringOfPlainText(match.fromPos, match.toPos),
        )
      )
      && !this.disabledRules.contains(match.ruleId)
    )
  }

  abstract fun isInitialized(): Boolean

  protected abstract fun checkInternal(
    annotatedTextFragment: AnnotatedTextFragment,
  ): List<LanguageToolRuleMatch>

  abstract fun activateDefaultFalseFriendRules()
  abstract fun activateLanguageModelRules(languageModelRulesDirectory: String)
  abstract fun enableRules(ruleIds: Set<String>)
  abstract fun enableEasterEgg()
}
