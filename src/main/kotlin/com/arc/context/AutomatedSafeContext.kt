
package com.arc.context

class AutomatedSafeContext(
    safeContext: SafeContext,
    automated: Automated
) : IAutomatedSafeContext, SafeContext by safeContext, Automated by automated