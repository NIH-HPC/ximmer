// Initialization stages - these just set specific output directories for each 
// analysis tool
init_excavator = { branch.dir="$batch_name/excavator"; branch.excavator_batch=batch_name }
init_xhmm = { branch.dir="$batch_name/xhmm"; branch.xhmm_batch_name=batch_name }
init_exome_depth = { branch.dir="$batch_name/exome_depth" }
init_cn_mops = { branch.dir="${batch_name}/cn_mops" }
init_conifer = { branch.dir="${batch_name}/conifer" }

create_analysable_target = {
    doc """
          Some callers cannot process chromosomes where the target region includes less targets than the number 
          of regions in the analysis. This stage removes those regions from the target region.
        """

    requires target_bed : "The target region BED file",
             sample_names : "The names of samples to analyse"

    def numSamples = sample_names.size()

    from(target_bed) filter('analysable') {

        groovy """
           INCLUDE_CHROMOSOMES = "${chromosomes.join(/,/)}".split(",")

           targetRegion = new BED("$input.bed", withExtra:true).load()

           chrs = targetRegion*.chr.unique()

           chrCounts = targetRegion.countBy { it.chr }

           filteredTargets = targetRegion.grep { 
               (it.chr in INCLUDE_CHROMOSOMES) && (chrCounts[it.chr] > $numSamples)
           } as Regions

           filteredTargets.save("$output.bed")
     
        """
   }
}

