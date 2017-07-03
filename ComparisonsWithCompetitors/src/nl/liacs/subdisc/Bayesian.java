package nl.liacs.subdisc;

import java.util.*;

public class Bayesian
{
	private DAG itsDAG;
	private BinaryTable itsTable;
	private static Random itsRandom;

	public Bayesian(BinaryTable theTable, List<Column> theTargets)
	{
		itsDAG = new DAG(theTargets);
		itsTable = theTable;
		itsRandom = new Random(System.currentTimeMillis()); // truly random
//		itsRandom = new Random(12345); // random, but always the same
	}

	public Bayesian(BinaryTable theTable)
	{
		itsDAG = new DAG(theTable.getNrColumns());
		itsTable = theTable;
		itsRandom = new Random(System.currentTimeMillis()); // truly random
//		itsRandom = new Random(12345); // random, but always the same
	}

	public DAG getDAG() { return itsDAG; }

	public void test()
	{
/*
		CrossCube aCube = new CrossCube(3);
		Log.logCommandLine("size: " + aCube.getSize());

		aCube.setCount(0, 15);
		aCube.setCount(1, 5);
		aCube.setCount(2, 5);
		aCube.setCount(3, 15);
		aCube.setCount(4, 5);
		aCube.setCount(5, 15);
		aCube.setCount(6, 15);
		aCube.setCount(7, 5);

		Log.logCommandLine("BDEU: " + aCube.getBDeu());

		aCube = new CrossCube(3);
		aCube.setCount(0, 15);
		aCube.setCount(1, 0);
		aCube.setCount(2, 0);
		aCube.setCount(3, 15);
		aCube.setCount(4, 0);
		aCube.setCount(5, 15);
		aCube.setCount(6, 15);
		aCube.setCount(7, 0);

		Log.logCommandLine("BDEU: " + aCube.getBDeu());

		aCube = new CrossCube(3);
		aCube.setCount(0, 0);
		aCube.setCount(1, 0);
		aCube.setCount(2, 15);
		aCube.setCount(3, 0);
		aCube.setCount(4, 0);
		aCube.setCount(5, 15);
		aCube.setCount(6, 0);
		aCube.setCount(7, 0);

		Log.logCommandLine("BDEU: " + aCube.getBDeu());
*/
		//scoreNext();
		climb();
	}

	EdgeQuality scoreNext()
	{
		EdgeQuality aQuality = new EdgeQuality();
		double qual;
		double bestQual = -100000;
		boolean chg = false;

		for(int i=0; i<itsDAG.getSize(); ++i)
			for(int j=0; j<i ; ++j)
			{

				switch (itsDAG.getNode(j).isConnected(i))
				{
				 case 0: //we have i-/-j (not connected)
					{
						if(itsDAG.addArcAcyclic(i, j, false))
						{
							qual = computeQuality(j);
							if ((qual - itsDAG.getQuality(j)) > bestQual)
							{
								bestQual = qual - itsDAG.getQuality(j);
								aQuality.setQuality(1, qual); //1 indicates addition
								aQuality.setEdge(i,j);
							}
							itsDAG.removeArc(i,j, false);
						}
						else
						{
							itsDAG.addArc(j, i, false);
							qual = computeQuality(i);
							if ((qual - itsDAG.getQuality(i)) > bestQual)
							{
								bestQual = qual - itsDAG.getQuality(i);
								aQuality.setQuality(1, qual); //1 indicates addition
								aQuality.setEdge(j, i);
							}
							itsDAG.removeArc(j, i, false);
							break;
						}

						if(itsDAG.addArcAcyclic(j, i, false))
						{
							qual = computeQuality(i);
							if ((qual - itsDAG.getQuality(i)) > bestQual)
							{
								bestQual = qual - itsDAG.getQuality(i);
								aQuality.setQuality(1, qual); //1 indicates addition
								aQuality.setEdge(j, i);
							}
							itsDAG.removeArc(j, i, false);
						}
					}
					break;

				 case 1: //we have i-->j
					 {
						int tmp = i;
						i = j;
						j = tmp;
						chg = true;
					 }
					// NO BREAK!

				 case 2: //we have i<--j
					{
						itsDAG.removeArc(j, i, false);
						qual = computeQuality(i);
						double qualTmp = qual;

						if ((qual - itsDAG.getQuality(i)) > bestQual)
						{
							bestQual = qual - itsDAG.getQuality(i);
							aQuality.setQuality(0, qual); //1 indicates removal
							aQuality.setEdge(j, i);
						}

						if(itsDAG.addArcAcyclic(i, j, false))
						{
							// only reverse non-covered arcs
							boolean covered = false;
							if (itsDAG.getNode(j).getNrParents()-1 == itsDAG.getNode(i).getNrParents())
							{
								ItemSet aDifferentParents = itsDAG.getNode(i).getParents().symmetricDifference(itsDAG.getNode(j).getParents());
								if ((aDifferentParents.getItemCount() == 1) && (aDifferentParents.get(i)))
								{
//									Log.logCommandLine("covered: " + itsDAG.getNode(i).getParents() + ", " + itsDAG.getNode(j).getParents());
									covered = true;
								}
							}

							if(!covered)
							{
								qual = computeQuality(j);
								if ((qual - itsDAG.getQuality(j)) + (qualTmp - itsDAG.getQuality(i)) > bestQual)
								{
									bestQual = (qual - itsDAG.getQuality(j)) + (qualTmp - itsDAG.getQuality(i));
									aQuality.setQuality(qualTmp, qual);
									aQuality.setEdge(i,j);
								}
							}

							itsDAG.removeArc(i, j, false);
						}

						itsDAG.addArc(j, i, false);

						if (chg)
						{
							int tmp = i;
							i = j;
							j = tmp;
							chg = false;
						}
					}
				}
			}

//		Log.logCommandLine("best: " + aQuality.getQuality());
		return aQuality;
	}

	private double computeQuality(int theChild)
	{
		BinaryTable aTable = itsTable.selectColumns(itsDAG.getNode(theChild).getParents());
		aTable.addColumn(itsTable.getColumn(theChild)); //make sure the child is the last column

		return aTable.computeBDeuFaster();
	}

	public void climb()
	{
		double modelQual = 0;
		double modelQualTmp;
		EdgeQuality best;
		boolean locMax = false;
		int trials = 0;
		int escape = 0;

		for(int i=0; i<itsDAG.getSize(); i++)
			modelQual += itsDAG.getQuality(i);

//		int aCycles = itsDAG.getSize() - 1; //a spanning tree if no cycles, otherwise less
		int aCycles = 2 * itsDAG.getSize(); //this is a setting that works for larger graphs, but more iterations is better, if you have the time.
//		int aCycles = 10 * itsDAG.getSize(); //this is the regular setting, if time is not an issue
		int cycle = aCycles;
		while(!locMax && cycle > 0)
		{
			cycle--;
//			if (cycle/10 == cycle/10.0f)
//				Log.logCommandLine("step " + (aCycles - cycle));

			modelQual += rcar(7);
			best = scoreNext(); //find the best single-arc change

			modelQualTmp = modelQual;
			modelQualTmp -= itsDAG.getQuality(best.getNode2()); //remove old quality from sum
			modelQualTmp += best.getQuality(); //add quality to sum
			if(best.getAction() < 0) //for reverse also for node 1
			{
				modelQualTmp -= itsDAG.getQuality(best.getNode1());
				modelQualTmp += best.getQuality();
			}


			locMax = (modelQualTmp < modelQual);
			if(!locMax)
			{
				trials=0;

				itsDAG.setQuality(best.getNode2(), best.getQuality()); //assign new quality for node 2
				switch ((int) best.getAction())
				{
					case 0:	//remove
						{
//							Log.logCommandLine("Removing " + best.getNode1() + " -> " + best.getNode2());
							itsDAG.removeArc(best.getNode1(), best.getNode2(), true);
						}
						break;

					case 1: //add
						{
//							Log.logCommandLine("Adding " + best.getNode1() + " -> " + best.getNode2());
							itsDAG.addArc(best.getNode1(), best.getNode2(), true);
						}
						break;

					default: //reverse
						{
							itsDAG.setQuality(best.getNode1(), best.getAction());
//							Log.logCommandLine("Reversing " + best.getNode2() + " -> " + best.getNode1());
							itsDAG.removeArc(best.getNode2(), best.getNode1(), true);
							itsDAG.addArc(best.getNode1(), best.getNode2(), true);
						}
				}

				modelQual=modelQualTmp;
			}
			else if (trials < 4) //MAXTRIAL
			{
				if (escape > 12) //MAXESCAPE
					return;
//				Log.logCommandLine("Local maximum - escape attempt nr: " + trials);
				modelQual += rcar(7);
				locMax = false;
				trials++;
				escape++;
			}
		}
	}

	public double rcar(int r)
	{
		BitSet chkThese = new BitSet(itsDAG.getSize());
		ArrayList<EdgeQuality> covEdges = new ArrayList<EdgeQuality>(50);
		int rr = (int) (itsRandom.nextFloat() * r);
		int e;

		for(int p=0; p<rr; p++)
		{
			for(int i=0; i<itsDAG.getSize(); ++i)
			{
				NetworkNode aChild = itsDAG.getNode(i);
				ItemSet aParents = aChild.getParents();

				for(int j=0; j<itsDAG.getSize(); j++)
					if (aParents.get(j)) // j is a parent of i
					{
						NetworkNode aParent = itsDAG.getNode(j);

						if (aChild.getNrParents()-1 == aParent.getNrParents())
						{
							ItemSet aDifferentParents = aChild.getParents().symmetricDifference(aParent.getParents());
							if ((aDifferentParents.getItemCount() == 1) && (aDifferentParents.get(j)))
							{
								EdgeQuality anEdge = new EdgeQuality();
								anEdge.setEdge(j, i);
								covEdges.add(anEdge);
//								Log.logCommandLine("covered: " + itsDAG.getNode(i).getParents() + ", " + itsDAG.getNode(j).getParents() + " " + j);
							}
						}
					}
			}

			if (covEdges.size() != 0)
			{
				e = (int) (itsRandom.nextFloat() * covEdges.size());
				EdgeQuality anEdge = covEdges.get(e);
				itsDAG.removeArc(anEdge.getNode1(), anEdge.getNode2(), false);
				itsDAG.addArc(anEdge.getNode2(), anEdge.getNode1(), false);
//				Log.logCommandLine("Reversing " + anEdge.getNode2() + " -> " + anEdge.getNode1());

				chkThese.set(anEdge.getNode1());
				chkThese.set(anEdge.getNode2());
			}
		}

		double scoreDiff=0;
		for(int nd=0; nd<chkThese.size(); nd++)
			if (chkThese.get(nd))
			{
				scoreDiff -= itsDAG.getQuality(nd);
				scoreDiff += computeQuality(nd);
			}

		return scoreDiff;
	}

	private class EdgeQuality
	{
		private double qualOne;
		private double qualTwo;
		private int itsNode1;
		private int itsNode2;

		public EdgeQuality()
		{
			qualOne = -100000;
			qualTwo = -100000;
		}

		public void setEdge(int i, int j)
		{
			itsNode1 = i;
			itsNode2 = j;
		}

		public void setQuality(double x, double y)
		{
			qualOne = x;
			qualTwo = y;
		}

		public double getQuality() { return qualTwo; }
		public double getAction() { return qualOne; }
		public int getNode1() { return itsNode1; }
		public int getNode2() { return itsNode2; }
	}


/*
	edgeQual scoreNext(void)
	{
		edgeQual quality;
		quality.qualOne=quality.qualTwo=-100000;
		double qual, bestQual=-100000;
		bool chg=false;

		for(unsigned i=0; i<itsDAG->getNumberOfNodes(); ++i)
			for(unsigned j=0; j<i ; ++j)
			{
				//std::cout << " nodes " << i << " " << j << "\n";

				switch (itsDAG->getNode(j)->isConnected(i))
				{
				 case 0: //we have i-/-j
					{
						if(itsDAG->addArcAcyclic(i,j))
						{
							db->counts(itsDAG->getNode(j)->parents(), j, itsDAG->getNode(j)->getParameters());
							qual=itsDAG->getNode(j)->getParameters()->calcBDeu();
							if ((qual-itsDAG->getNode(j)->qualNode)>bestQual)
							{
								bestQual=qual-itsDAG->getNode(j)->qualNode;
								quality.qualOne=1; //1 indicates addition
								quality.qualTwo=qual;
								quality.edge=std::make_pair(i,j);
							}
							itsDAG->removeArc(i,j);
						} else
						{
							itsDAG->addArc(j,i);
							db->counts(itsDAG->getNode(i)->parents(), i, itsDAG->getNode(i)->getParameters());
							qual=itsDAG->getNode(i)->getParameters()->calcBDeu();
							if ((qual-itsDAG->getNode(i)->qualNode)>bestQual)
							{
								bestQual=qual-itsDAG->getNode(i)->qualNode;
								quality.qualOne=1;
								quality.qualTwo=qual;
								quality.edge=std::make_pair(j,i);
							}
							itsDAG->removeArc(j,i);
							break;
						}

						if(itsDAG->addArcAcyclic(j,i))
						{
							db->counts(itsDAG->getNode(i)->parents(), i, itsDAG->getNode(i)->getParameters());
							qual=itsDAG->getNode(i)->getParameters()->calcBDeu();
							if ((qual-itsDAG->getNode(i)->qualNode)>bestQual)
							{
								bestQual=qual-itsDAG->getNode(i)->qualNode;
								quality.qualOne=1;
								quality.qualTwo=qual;
								quality.edge=std::make_pair(j,i);
							}
							itsDAG->removeArc(j,i);
						}
					}
					break;

				 case 1: //we have i-->j
					 {
						unsigned tmp=i;
						i=j;
						j=tmp;
						chg=true;
					 }

				 case 2: //we have i<--j
					{
						itsDAG->removeArc(j,i);
						db->counts(itsDAG->getNode(i)->parents(), i, itsDAG->getNode(i)->getParameters());
						qual=itsDAG->getNode(i)->getParameters()->calcBDeu();
						double qualTmp=qual;
						if ((qual-itsDAG->getNode(i)->qualNode)>bestQual)
						{
							bestQual=qual-itsDAG->getNode(i)->qualNode;
							quality.qualOne=0; //0 indicates removal
							quality.qualTwo=qual;
							quality.edge=std::make_pair(j,i);
						}

						if(itsDAG->addArcAcyclic(i,j, true))
						{
							// only reverse non-covered arcs
							bool covered=false;
							if (itsDAG->getNode(j)->parents().size()-1==itsDAG->getNode(i)->parents().size())
							{
								std::set<unsigned short> diffParents;
								std::insert_iterator<std::set<unsigned short> >  res_ins(diffParents, diffParents.begin());
								std::set_symmetric_difference(itsDAG->getNode(i)->parents().begin(), itsDAG->getNode(i)->parents().end(), itsDAG->getNode(j)->parents().begin(), itsDAG->getNode(j)->parents().end(), res_ins);
								if ((diffParents.size()==1) && (*diffParents.begin()==i)) covered=true;
							}

							if(!covered)
							{
								itsDAG->fixDimensions(j);
								db->counts(itsDAG->getNode(j)->parents(), j, itsDAG->getNode(j)->getParameters());
								qual=itsDAG->getNode(j)->getParameters()->calcBDeu();
								if ((qual-itsDAG->getNode(j)->qualNode)+(qualTmp-itsDAG->getNode(i)->qualNode)>bestQual)
								{
									bestQual=(qual-itsDAG->getNode(j)->qualNode)+(qualTmp-itsDAG->getNode(i)->qualNode);
									quality.qualOne=qualTmp;
									quality.qualTwo=qual;
									quality.edge=std::make_pair(i,j);
								}
							}

							itsDAG->removeArc(i,j, true);
						}

						itsDAG->addArc(j,i);

						if (chg)
						{
							unsigned tmp=i;
							i=j;
							j=tmp;
							chg=false;
						}
					}
				}
			}

		return quality;
	}

	double rcar(unsigned short r)
	{
		std::vector<bool> chkThese(itsDAG->getNumberOfNodes(),false);
		std::vector<std::pair<unsigned, unsigned> > covEdges;
		covEdges.reserve(50);
		RandDiscreteUniform rd(0,r,1,rand());
		unsigned short rr = rd.RandValue();
		unsigned int e;
		std::set<unsigned short> diffParents;

		for(unsigned short p=0; p<rr; ++p)	 //was rr
		{
			for(unsigned short i=0; i<itsDAG->getNumberOfNodes(); ++i)
				for(itNodes l=itsDAG->getNode(i)->parents().begin(); l!=itsDAG->getNode(i)->parents().end(); ++l)
				{
					if (itsDAG->getNode(i)->parents().size()-1==itsDAG->getNode(*l)->parents().size())
					{
						diffParents.clear();
						std::insert_iterator<std::set<unsigned short> >  res_ins(diffParents, diffParents.begin());
						std::set_symmetric_difference(itsDAG->getNode(i)->parents().begin(), itsDAG->getNode(i)->parents().end(), itsDAG->getNode(*l)->parents().begin(), itsDAG->getNode(*l)->parents().end(), res_ins);
						if ((diffParents.size()==1) && (*diffParents.begin()==*l))
							covEdges.push_back(std::make_pair(*l,i));
					}
				}

			if (covEdges.size()!=0)
			{
				RandDiscreteUniform rd(0,covEdges.size()-1,1,rand());
				e = rd.RandValue();
				itsDAG->removeArc(covEdges[e].first, covEdges[e].second, true);
				itsDAG->addArc(covEdges[e].second, covEdges[e].first, true);
				chkThese[covEdges[e].first]=chkThese[covEdges[e].second]=true;

				covEdges.resize(0);
			}
		}

		double scoreDiff=0;
		for(unsigned short nd=0; nd<chkThese.size(); ++nd)
			if (chkThese[nd])
			{
				scoreDiff-=itsDAG->getNode(nd)->qualNode;
				itsDAG->fixDimensions(nd);
				db->counts(itsDAG->getNode(nd)->parents(), nd, itsDAG->getNode(nd)->getParameters());
				itsDAG->getNode(nd)->qualNode=itsDAG->getNode(nd)->getParameters()->calcBDeu();
				scoreDiff+=itsDAG->getNode(nd)->qualNode;
			}

		return scoreDiff;
	}
*/
}