package cmu.isr.app

import cmu.isr.robustify.supervisory.Algorithms
import com.fasterxml.jackson.annotation.JsonProperty

enum class SolverType { DESops, Supremica }

data class RobustifyConfigJSON(
  @JsonProperty
  val sys: List<String>,
  @JsonProperty
  val env: List<String>,
  @JsonProperty
  val dev: List<String>,
  @JsonProperty
  val safety: List<String>,
  @JsonProperty
  val method: String,
  @JsonProperty
  val options: RobustifyOptionsJSON = RobustifyOptionsJSON()
)

data class RobustifyOptionsJSON(
  @JsonProperty
  val progress: List<String> = emptyList(),
  @JsonProperty
  val preferred: List<List<String>> = emptyList(),
  @JsonProperty
  val preferredMap: Map<String, List<List<String>>> = emptyMap(),
  @JsonProperty
  val controllable: List<String> = emptyList(),
  @JsonProperty
  val controllableMap: Map<String, List<String>> = emptyMap(),
  @JsonProperty
  val observable: List<String> = emptyList(),
  @JsonProperty
  val observableMap: Map<String, List<String>> = emptyMap(),
  @JsonProperty
  val solver: String = SolverType.Supremica.name,
  @JsonProperty
  val algorithm: String = Algorithms.Pareto.name,
  @JsonProperty
  val maxIter: Int = 1
)