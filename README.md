# Gengen (version 2)

Gengen is a generator of naming languages for fantasy worlds, designed as a tool for authors, game masters, and other world builders. Each language generated in Gengen has its own set of phonological rules and statistics and therefore its own unique sound, producing names that feel phonetically similar to each other, but distinct from those produced by the next language.

Gengen broadly functions by creating rules for generating syllables, stringing syllables together to produce names, then using metrical rules to assign primary and secondary stresses. The rough outline of the procedure is as follows:
- Assignment of basic syllable structure parameters
- Construction of phonemic inventory through random phonetic feature selection
- Identification of valid phoneme sequences through application of phonotactic rules
- Creation of rules for stress assignment
- Calculation of probabilities for guiding the name assembly state machine
- Entropy measurements for determining word length
Names can then be assembled through the state machine one syllable at a time until the desired information content is reached.

You can try Gengen in your browser at https://tunditur-unda.itch.io/gengen.
