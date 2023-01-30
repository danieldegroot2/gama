/**
* Name: OLZ_Controler
* MPI Controler of OLZ.gaml
* Author: Lucas GROSJEAN
* Tags: MPI, Controler
*/

model controler

import "OLZ.gaml" as pp

global
{
	
	string file_name;
	int final_step <- 25;
	int grid_widht <- 2;
	int grid_lenght <- 2;
	
	int rank;
	
	init
	{	
		seed <- 13.0;
		do init_sub_simulation;
		create slave;
	}
    
    action init_sub_simulation
    {
    	create pp.movingExp;
    }
    
    reflex runModel when: cycle < final_step
    {
		ask (pp.movingExp collect each.simulation)
	    {
			do _step_;
	    }
    }
    
    reflex die when: cycle = final_step
    {
    	ask (pp.movingExp collect each.simulation)
	    {
			do die;
	    }
	    ask slave
	    {
	    	do die;
	    }
	    
	    do die;
    }
}

species slave parent: SlaveMPI
{
	cell cellule;
	list<int> list_of_neighbors;
	map<int, geometry> list_of_Rank_Outer_Geo;
	
	list<agent> agents_from_model; 
	list<agent> inside_main -> { agents_from_model inside(shape) }; // agents in my shape
	list<agent> inside_outer_OLZ -> { agents_from_model inside(cellule.outer_OLZ) }; // agents in my outer OLZ (neighbors OLZ)
	list<agent> inside_me -> { inside_main + inside_outer_OLZ }; // agents inside me (can be copy)
	list<agent> outside_me -> { not( agents_from_model in (inside_me) )}; // agents that are going to leave the current process to another
	
	int division;
	int remainder;
	
	init
	{
		rank <- myRank;
		file_name <- "log"+myRank+".txt";
		do clearLogFile();
		
		division <- myRank / grid_lenght;
		remainder <- myRank mod grid_lenght;
		
		
		do writeLog("My rank is " + myRank);
		do writeLog("Grid size is [" + grid_lenght + "," + grid_widht + "]");
		do writeLog("My position is [" + division + "," + remainder + "]");
		do writeLog("NetSize " + MPI_SIZE());
		do writeLog("Seed = " + seed);	
		
		list<int> tmp_list;
		map<int, geometry> list_pair;
		
		ask pp.movingExp[0]
		{
			myself.cellule <- cell[myself.division, myself.remainder];
			myself.shape <- myself.cellule.shape;
			myself.agents_from_model <- agents;
			
			cell tmp <- myself.cellule;
			
			ask myself.cellule.neighbors
			{
				geometry intersect <- tmp.inner_OLZ inter self.outer_OLZ;
				int index <- self.cell_index;
				
				add index to: tmp_list;
				add index :: intersect to: list_pair;
			}
		}
		
		
		list_of_neighbors <- tmp_list;
		map_neighbor_innerOLZ <- list_pair;
		
		do writeLog("list_of_geo = " + map_neighbor_innerOLZ);
		do writeLog("neighbor = " + list_of_neighbors);
		
		do deleteAgentsNotInMyArea;
		do registerAgentInLocationManager(inside_main);
	}
	
	action deleteAgentsNotInMyArea
	{
		do writeLog("-"+deleteAgentsNotInMyArea+"-");
		
		list<agent> agent_outside_me;
		string deleted <- "";
		ask pp.movingExp[0] 
	    {
	    	agent_outside_me <- agents - myself.inside_me;
	    	
	    	ask movingAgent// important to specify the type to not delete experiment agent for example
	    	{
	    		if(agent_outside_me contains self)
	    		{
	    			deleted<- deleted + ", " + self.name;
	    			do die;
	    		}
	    	}
	    }
	    
		do writeLog("deleted = " + deleted);
		do writeLog("inside_main" + inside_main);
		do writeLog("inside_outer_OLZ" + inside_outer_OLZ);
		do writeLog("agent_inside_me" + inside_me);
		do writeLog("NB AGENTS agent_inside_me =  " +length(inside_me));
	}
	
	reflex getAgentFromModel
	{
		ask pp.movingExp[0]
	    {
			myself.agents_from_model <- agents; 
		}
	}
	
	reflex
	{
		ask inside_me
		{
			agent tmp <- self;
			ask myself
			{
				do writeLog(""+tmp.name+"(uniqueID) = " + tmp.initialMpiRank + " :: " +tmp.uniqueID);
			}
		}
	}
	
	reflex routineMPI
	{
		do writeLog("--------"+cycle+"----------");

		do start_listener;
		do MPI_BARRIER();
		
		
		loop neighbor over: list_of_neighbors
		{
			list<agent> t <- getAgentInNeighborInnerOLZ(neighbor);		
			do writeLog("getAgentInNeighborInnerOLZ N°" + neighbor + "  result = " + t);
			
			// get all agents inside neighbor shape but not in OLZ
			// send them
		}
		
		do MPI_BARRIER();
		do stop_listener;
		
		
	}
	
    action writeLog(string log)
	{
		save log type: text to: file_name rewrite:false;
	}
	
	action clearLogFile
	{
		save "" type: text to: file_name rewrite:true;
	}
}


experiment main
{	
}